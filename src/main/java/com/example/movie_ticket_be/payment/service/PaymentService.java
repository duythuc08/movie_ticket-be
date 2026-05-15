
package com.example.movie_ticket_be.payment.service;

import com.example.movie_ticket_be.auth.service.EmailService;
import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.booking.enums.TicketStatus;
import com.example.movie_ticket_be.booking.repository.OrderFoodRepository;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.booking.repository.OrderTicketRepository;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.core.utils.QRCodeUtils;
import com.example.movie_ticket_be.payment.dto.request.PaymentConfirmRequest;
import com.example.movie_ticket_be.payment.entity.Payments;
import com.example.movie_ticket_be.payment.enums.PaymentStatus;
import com.example.movie_ticket_be.payment.repository.PaymentRepository;
import com.example.movie_ticket_be.showtime.entity.SeatShowTime;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import com.example.movie_ticket_be.showtime.repository.SeatShowTimeRepository;
import com.example.movie_ticket_be.user.entity.LoyaltyPointsHistory;
import com.example.movie_ticket_be.user.entity.MembershipTier;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.repository.LoyaltyPointsHistoryRepository;
import com.example.movie_ticket_be.user.repository.MembershipTierRepository;
import com.example.movie_ticket_be.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
    OrderRepository orderRepository;
    OrderTicketRepository orderTicketRepository;
    OrderFoodRepository orderFoodRepository;
    PaymentRepository paymentRepository;
    QRCodeUtils qrCodeUtils;
    SeatShowTimeRepository seatShowTimeRepository;
    UserRepository userRepository;
    MembershipTierRepository membershipTierRepository;
    LoyaltyPointsHistoryRepository pointsHistoryRepository;

    EmailService emailService;

    @Transactional
    public void processSuccess(PaymentConfirmRequest request){
        Orders order = orderRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        //a. Lưu bản ghi thanh toán
        Payments payment = Payments.builder()
                .order(order)
                .amount(order.getFinalPrice())
                .paymentDate(LocalDateTime.now())
                .transactionId(request.getTransactionId())
                .paymentInfo(request.getPaymentInfo())
                .paymentType(request.getPaymentType())
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();
        paymentRepository.save(payment);

        //b. cập nhật order tạo qrCode
        order.setOrderStatus(OrderStatus.PAID);
        String qrCode = "INF-" + order.getOrderId() + "-" + System.currentTimeMillis() % 10000;
        String qrBase64 = qrCodeUtils.generateQRCodeImage(qrCode,300,300);
        order.setQrCode(qrCode);
        orderRepository.save(order);

        //c. Cập nhật vé và ghế
        List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(order.getOrderId());
        for (OrderTickets t : tickets) {
            t.setTicketStatus(TicketStatus.CONFIRMED);

            SeatShowTime sst = t.getSeatShowTime();
            sst.setSeatShowTimeStatus(SeatShowTimeStatus.SOLD);
            sst.setLockedUntil(null);
            seatShowTimeRepository.save(sst);
        }
        orderTicketRepository.saveAll(tickets);

        //d. tích điểm & thăng hạng
        updateTicketsAndSeats(order);
        handleUserLoyalty(order);

        //.e Gửi mail thông báo đặt vé
        if(order.getUsers() != null) {
            sendPaymentSuccessMail(order.getUsers(), order, qrCode, qrBase64);
        }
    }

    @Transactional
    public void processFail(Orders order){
        //a. Cập nhật Order
        order.setOrderStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);

        //b. Cập nhật Ticket & TRẢ GHẾ
        List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(order.getOrderId());
        for (OrderTickets ticket : tickets) {
            ticket.setTicketStatus(TicketStatus.CANCELLED);

            SeatShowTime sst = ticket.getSeatShowTime();
            sst.setSeatShowTimeStatus(SeatShowTimeStatus.AVAILABLE); // Nhả ghế về trạng thái trống
            sst.setLockedUntil(null); // Xóa thời gian giữ ghế
            seatShowTimeRepository.save(sst);
        }
        orderTicketRepository.saveAll(tickets);
    }

    private void handleUserLoyalty(Orders order) {
        Users user = order.getUsers();
        if (user == null || order.getPointsEarned() <= 0) return;

        // 1. Lưu số dư cũ
        int oldBalance = user.getLoyaltyPoints();
        int pointsToEarn = order.getPointsEarned();
        int newBalance = oldBalance + pointsToEarn;

        // 2. Cập nhật điểm cho User
        user.setLoyaltyPoints(newBalance);

        // 3. Lưu lịch sử biến động điểm
        LoyaltyPointsHistory history = LoyaltyPointsHistory.builder()
                .user(user)
                .order(order)
                .pointsChange(pointsToEarn)
                .oldBalance(oldBalance)
                .newBalance(newBalance)
                .description("Tích điểm từ đơn hàng #" + order.getOrderId())
                .createdAt(LocalDateTime.now())
                .build();
        pointsHistoryRepository.save(history);

        // 4. Kiểm tra và thăng hạng tự động
        checkAndUpgradeMembership(user);

        userRepository.save(user);
    }

    private void checkAndUpgradeMembership(Users user) {
        List<MembershipTier> tiers = membershipTierRepository.findAllOrderByPointsRequiredDesc();

        for (MembershipTier tier : tiers) {
            if (user.getLoyaltyPoints() >= tier.getPointsRequired()) {
                if (user.getMembershipTier() == null ||
                        tier.getPointsRequired() > user.getMembershipTier().getPointsRequired()) {

                    log.info("User {} thăng hạng lên: {}", user.getUsername(), tier.getName());
                    user.setMembershipTier(tier);
                }
                break;
            }
        }
    }

    private void updateTicketsAndSeats(Orders order) {
        List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(order.getOrderId());
        for (OrderTickets t : tickets) {
            t.setTicketStatus(TicketStatus.CONFIRMED);
            SeatShowTime sst = t.getSeatShowTime();
            sst.setSeatShowTimeStatus(SeatShowTimeStatus.SOLD);
            sst.setLockedUntil(null);
            seatShowTimeRepository.save(sst);
        }
        orderTicketRepository.saveAll(tickets);
    }
    private void sendPaymentSuccessMail(Users user, Orders order, String bookingCode, String qrBase64) {
        String htmlMessage = "<html>"
                + "<body style=\"font-family: Arial, sans-serif; background-color: #f5f5f5; padding: 20px;\">"
                + "<div style=\"max-width: 600px; margin: auto; background-color: #fff; padding: 20px; border-radius: 8px; "
                + "box-shadow: 0 0 10px rgba(0,0,0,0.1);\">"
                + "<h2 style=\"color: #007bff; text-align: center;\">Thanh toán thành công 🎉</h2>"
                + "<p>Xin chào <b>" + user.getFirstname() + " " + user.getLastname() + "</b>,</p>"
                + "<p>Cảm ơn bạn đã đặt vé tại <b>Infinity Cinema</b>. Dưới đây là thông tin đơn hàng của bạn:</p>"
                + "<ul>"
                + "<li><b>Mã đơn hàng:</b> " + order.getOrderId() + "</li>"
                + "<li><b>Mã booking:</b> " + bookingCode + "</li>"
                + "<li><b>Số tiền:</b> " + order.getFinalPrice() + " VND</li>"
                + "<li><b>Thời gian thanh toán:</b> " + LocalDateTime.now() + "</li>"
                + "</ul>"
                + "<p>Vui lòng sử dụng mã QR dưới đây để check-in tại rạp:</p>"
                + "<div style=\"text-align: center; margin: 20px;\">"
                + "<img src=\"cid:qrCodeImage\" alt=\"QR Code\" style=\"width:200px;height:200px;\"/>" // Thay đổi ở đây
                + "</div>"
                + "<p style=\"text-align: center; font-size: 14px; color: #555;\">Chúc bạn có trải nghiệm xem phim tuyệt vời!</p>"
                + "</div>"
                + "</body>"
                + "</html>";

        try {
            byte[] qrBytes = java.util.Base64.getDecoder().decode(qrBase64);

            emailService.sendEmailWithInlineImage(
                    user.getUsername(),
                    "Xác nhận thanh toán thành công",
                    htmlMessage,
                    qrBytes
            );
        } catch (Exception e) {
            log.error("Failed to send payment success email", e);
        }
    }

}
