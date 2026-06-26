
package com.example.movie_ticket_be.payment.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.auth.service.EmailService;
import com.example.movie_ticket_be.booking.entity.OrderFoods;
import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.booking.enums.TicketStatus;
import com.example.movie_ticket_be.booking.repository.OrderFoodRepository;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.booking.repository.OrderTicketRepository;
import com.example.movie_ticket_be.cinema.entity.Foods;
import com.example.movie_ticket_be.cinema.enums.FoodStatus;
import com.example.movie_ticket_be.cinema.repository.FoodRepository;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.core.utils.QRCodeUtils;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.payment.dto.request.PaymentConfirmRequest;
import com.example.movie_ticket_be.payment.entity.Payments;
import com.example.movie_ticket_be.payment.enums.PaymentStatus;
import com.example.movie_ticket_be.payment.enums.PaymentType;
import com.example.movie_ticket_be.payment.repository.PaymentRepository;
import com.example.movie_ticket_be.promotion.repository.PromotionRepository;
import com.example.movie_ticket_be.recommendation.dto.request.ActivityLogRequest;
import com.example.movie_ticket_be.recommendation.enums.ActionType;
import com.example.movie_ticket_be.recommendation.service.UserActivityLogService;
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

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PaymentService {
	OrderRepository orderRepository;
	OrderTicketRepository orderTicketRepository;
	OrderFoodRepository orderFoodRepository;
	FoodRepository foodRepository;
	PaymentRepository paymentRepository;
	QRCodeUtils qrCodeUtils;
	SeatShowTimeRepository seatShowTimeRepository;
	UserRepository userRepository;
	MembershipTierRepository membershipTierRepository;
	LoyaltyPointsHistoryRepository pointsHistoryRepository;
	UserActivityLogService userActivityLogService;
	EmailService emailService;
	PromotionRepository promotionRepository;

	@Transactional
	public void createPendingPayment(Long orderId, PaymentType paymentType) {
		Orders order = orderRepository.findByOrderId(orderId)
				.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
		Payments payment = Payments.builder()
				.order(order)
				.amount(order.getFinalPrice())
				.paymentType(paymentType)
				.paymentStatus(PaymentStatus.PENDING)
				.build();
		paymentRepository.save(payment);
	}

	@Transactional
	public void processSuccess(PaymentConfirmRequest request) {
		Orders order = orderRepository.findByOrderId(request.getOrderId())
				.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
		if (order.getOrderStatus() != OrderStatus.PENDING
				&& order.getOrderStatus() != OrderStatus.IN_PROGRESS) return;
		// a. Cập nhật bản ghi PENDING → SUCCESS (hoặc tạo mới nếu không tồn tại)
		Payments payment = paymentRepository
				.findByOrder_OrderIdAndPaymentStatus(order.getOrderId(), PaymentStatus.PENDING)
				.orElse(Payments.builder().order(order).amount(order.getFinalPrice()).build());
		payment.setPaymentDate(LocalDateTime.now());
		payment.setTransactionId(request.getTransactionId());
		payment.setPaymentInfo(request.getPaymentInfo());
		payment.setPaymentType(request.getPaymentType());
		payment.setPaymentStatus(PaymentStatus.SUCCESS);
		paymentRepository.save(payment);

		// b. cập nhật order tạo qrCode
		order.setOrderStatus(OrderStatus.PAID);
		String qrCode = "INF-" + order.getOrderId() + "-" + System.currentTimeMillis() % 10000;
		String qrBase64 = qrCodeUtils.generateQRCodeImage(qrCode, 300, 300);
		order.setQrCode(qrCode);
		orderRepository.save(order);
        logBookTicket(order);
		// c. Cập nhật vé và ghế
		List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(order.getOrderId());
		for (OrderTickets t : tickets) {
			t.setTicketStatus(TicketStatus.CONFIRMED);

			SeatShowTime sst = t.getSeatShowTime();
			sst.setSeatShowTimeStatus(SeatShowTimeStatus.SOLD);
			sst.setLockedUntil(null);
			seatShowTimeRepository.save(sst);
		}
		orderTicketRepository.saveAll(tickets);

		// d. tích điểm & thăng hạng
		updateTicketsAndSeats(order);
		handleUserLoyalty(order);

		// .e Gửi mail thông báo đặt vé
		if (order.getUsers() != null) {
			sendPaymentSuccessMail(order.getUsers(), order, qrCode, qrBase64);
		}

	}

	private void logBookTicket(Orders order) {
		if (order.getUsers() == null) return;
		Movies movie = order.getOrderTickets().stream()
				.findFirst()
				.map(t -> t.getSeatShowTime().getShowTimes().getMovies())
				.orElse(null);
		List<String> seatNames = order.getOrderTickets().stream()
				.map(t -> t.getSeatShowTime().getSeats().getSeatRow() + t.getSeatShowTime().getSeats().getSeatNumber())
				.toList();
		userActivityLogService.logInternal(ActivityLogRequest.builder()
				.actionType(ActionType.BOOK_TICKET)
				.movieId(movie != null ? movie.getMovieId() : null)
				.metadata(java.util.Map.of(
						"orderId", order.getOrderId(),
						"amount", order.getFinalPrice(),
						"seats", seatNames
				))
				.build(), order.getUsers());
	}

	@Transactional
	public void processFail(Orders order) {
		processFail(order, OrderStatus.CANCELLED);
	}

	@Transactional
	public void processFail(Orders order, OrderStatus status) {
		if (order.getOrderStatus() != OrderStatus.PENDING
				&& order.getOrderStatus() != OrderStatus.IN_PROGRESS) return;
		// a. Cập nhật Order
		order.setOrderStatus(status);
		orderRepository.save(order);

		// a2. Cập nhật Payment PENDING → FAILED
		paymentRepository.findByOrder_OrderIdAndPaymentStatus(order.getOrderId(), PaymentStatus.PENDING)
				.ifPresent(payment -> {
					payment.setPaymentStatus(PaymentStatus.FAILED);
					payment.setPaymentDate(LocalDateTime.now());
					paymentRepository.save(payment);
				});

		// b. Cập nhật Ticket & TRẢ GHẾ
		List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(order.getOrderId());
		for (OrderTickets ticket : tickets) {
			ticket.setTicketStatus(TicketStatus.CANCELLED);

			SeatShowTime sst = ticket.getSeatShowTime();
			sst.setSeatShowTimeStatus(SeatShowTimeStatus.AVAILABLE);
			sst.setLockedUntil(null);
			seatShowTimeRepository.save(sst);
		}
		orderTicketRepository.saveAll(tickets);

		// c. Hoàn tồn kho food
		List<OrderFoods> orderFoods = orderFoodRepository.findByOrders_OrderId(order.getOrderId());
		for (OrderFoods orderFood : orderFoods) {
			Foods food = orderFood.getFoods();
			int restoredStock = food.getStockQuantity() + orderFood.getQuantity();
			food.setStockQuantity(restoredStock);
			if (food.getFoodStatus() == FoodStatus.OUT_OF_STOCK) {
				food.setFoodStatus(FoodStatus.IN_STOCK);
				food.setEntityStatus(EntityStatus.ACTIVE);
			}
		}
		foodRepository.saveAll(orderFoods.stream().map(OrderFoods::getFoods).toList());

		// d. Hoàn lại useLimit của promotion nếu có
		if (order.getPromotionCode() != null) {
			promotionRepository.findByCodeIgnoreCase(order.getPromotionCode())
					.ifPresent(p -> {
						if (p.getUseLimit() != null) p.setUseLimit(p.getUseLimit() + 1);
						promotionRepository.save(p);
					});
		}

		// d. Log tín hiệu âm
		ActionType actionType = (status == OrderStatus.EXPIRED)
				? ActionType.TIMEOUT_HOLD_SEATS
				: ActionType.CANCEL_PAYMENT;
		tickets.stream().findFirst()
				.map(t -> t.getSeatShowTime().getShowTimes().getMovies().getMovieId())
				.ifPresent(movieId -> userActivityLogService.logInternal(
						ActivityLogRequest.builder().actionType(actionType).movieId(movieId).build(),
						order.getUsers()));
	}

	private void handleUserLoyalty(Orders order) {
		Users user = order.getUsers();
		if (user == null || order.getPointsEarned() <= 0)
			return;

		// 1. Lưu số dư cũ
		int oldBalance = user.getLoyaltyPoints();
		int pointsToEarn = order.getPointsEarned();
		int newBalance = oldBalance + pointsToEarn;

		// 2. Cập nhật điểm cho User
		user.setLoyaltyPoints(newBalance);

		// 3. Lưu lịch sử biến động điểm
		LoyaltyPointsHistory history = LoyaltyPointsHistory.builder().user(user).order(order).pointsChange(pointsToEarn)
				.oldBalance(oldBalance).newBalance(newBalance)
				.description("Tích điểm từ đơn hàng #" + order.getOrderId()).createdAt(LocalDateTime.now()).build();
		pointsHistoryRepository.save(history);

		// 4. Kiểm tra và thăng hạng tự động
		checkAndUpgradeMembership(user);

		userRepository.save(user);
	}

	private void checkAndUpgradeMembership(Users user) {
		List<MembershipTier> tiers = membershipTierRepository.findAllOrderByPointsRequiredDesc();

		for (MembershipTier tier : tiers) {
			if (user.getLoyaltyPoints() >= tier.getPointsRequired()) {
				if (user.getMembershipTier() == null
						|| tier.getPointsRequired() > user.getMembershipTier().getPointsRequired()) {

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
				+ "<h2 style=\"color: #007bff; text-align: center;\">Thanh toán thành công 🎉</h2>" + "<p>Xin chào <b>"
				+ user.getFirstname() + " " + user.getLastname() + "</b>,</p>"
				+ "<p>Cảm ơn bạn đã đặt vé tại <b>Infinity Cinema</b>. Dưới đây là thông tin đơn hàng của bạn:</p>"
				+ "<ul>" + "<li><b>Mã đơn hàng:</b> " + order.getOrderId() + "</li>" + "<li><b>Mã booking:</b> "
				+ bookingCode + "</li>" + "<li><b>Số tiền:</b> " + order.getFinalPrice() + " VND</li>"
				+ "<li><b>Thời gian thanh toán:</b> " + LocalDateTime.now() + "</li>" + "</ul>"
				+ "<p>Vui lòng sử dụng mã QR dưới đây để check-in tại rạp:</p>"
				+ "<div style=\"text-align: center; margin: 20px;\">"
				+ "<img src=\"cid:qrCodeImage\" alt=\"QR Code\" style=\"width:200px;height:200px;\"/>" // Thay đổi ở đây
				+ "</div>"
				+ "<p style=\"text-align: center; font-size: 14px; color: #555;\">Chúc bạn có trải nghiệm xem phim tuyệt vời!</p>"
				+ "</div>" + "</body>" + "</html>";

		try {
			byte[] qrBytes = java.util.Base64.getDecoder().decode(qrBase64);

			emailService.sendEmailWithInlineImage(user.getUsername(), "Xác nhận thanh toán thành công", htmlMessage,
					qrBytes);
		} catch (Exception e) {
			log.error("Failed to send payment success email", e);
		}
	}

}
