package com.example.movie_ticket_be.booking.service;

import com.example.movie_ticket_be.booking.dto.response.*;
import com.example.movie_ticket_be.booking.entity.OrderFoods;
import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.cinema.entity.Seats;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.payment.repository.PaymentRepository;
import com.example.movie_ticket_be.showtime.entity.SeatShowTime;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminOrderService {

    OrderRepository orderRepository;
    PaymentRepository paymentRepository;

    @Transactional
    public Page<AdminOrderSummaryResponse> getOrders(Specification<Orders> spec, Pageable pageable) {
        return orderRepository.findAll(spec, pageable).map(this::toSummaryResponse);
    }

    @Transactional
    public OrderResponse getOrderDetail(Long orderId) {
        Orders order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        String userId = null, fullName = "---";
        if (order.getUsers() != null) {
            userId = order.getUsers().getUserId();
            fullName = order.getUsers().getFirstname() + " " + order.getUsers().getLastname();
        }

        ShowTimeInfo showTimeInfo = buildShowTimeInfo(order);

        List<OrderTicketResponse> ticketResponses = buildTicketResponses(order);
        List<OrderFoodResponse> foodResponses = buildFoodResponses(order);

        PaymentResponse paymentResponse = paymentRepository.findByOrder_OrderId(orderId)
                .map(p -> PaymentResponse.builder()
                        .paymentId(p.getPaymentId())
                        .amount(p.getAmount())
                        .transactionId(p.getTransactionId())
                        .paymentInfo(p.getPaymentInfo())
                        .paymentDate(p.getPaymentDate())
                        .paymentType(p.getPaymentType())
                        .paymentStatus(p.getPaymentStatus())
                        .build())
                .orElse(null);

        return OrderResponse.builder()
                .orderId(order.getOrderId())
                .userId(userId)
                .fullName(fullName)
                .showTimeInfo(showTimeInfo)
                .totalTicketPrice(order.getTotalTicketPrice())
                .totalFoodPrice(order.getTotalFoodPrice())
                .memberDiscountAmount(order.getMemberDiscountAmount())
                .discountAmount(order.getDiscountAmount())
                .promotionCode(order.getPromotionCode())
                .finalPrice(order.getFinalPrice())
                .pointsEarned(order.getPointsEarned())
                .orderStatus(order.getOrderStatus())
                .bookingTime(order.getBookingTime())
                .expiredTime(order.getExpiredTime())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .qrCode(order.getQrCode())
                .tickets(ticketResponses)
                .foods(foodResponses)
                .payment(paymentResponse)
                .build();
    }

    @Transactional
    public void checkin(Long orderId, String qrCode) {
        Orders order = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        if (order.getOrderStatus() == OrderStatus.USED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_USED);
        }
        if (order.getOrderStatus() != OrderStatus.PAID) {
            throw new AppException(ErrorCode.ORDER_CANNOT_CHECKIN);
        }
        if (!qrCode.equals(order.getQrCode())) {
            throw new AppException(ErrorCode.INVALID_QR_CODE);
        }
        order.setOrderStatus(OrderStatus.USED);
        orderRepository.save(order);
    }

    @Transactional
    public AdminOrderStatsResponse getStats(LocalDateTime from, LocalDateTime to) {
        BigDecimal totalRevenue = Optional.ofNullable(
                orderRepository.sumRevenue(OrderStatus.PAID, from, to)).orElse(BigDecimal.ZERO);
        return AdminOrderStatsResponse.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(orderRepository.countByPeriod(from, to))
                .paidOrders(orderRepository.countByStatusAndPeriod(OrderStatus.PAID, from, to))
                .cancelledOrders(orderRepository.countByStatusAndPeriod(OrderStatus.CANCELLED, from, to))
                .pendingOrders(orderRepository.countByStatusAndPeriod(OrderStatus.PENDING, from, to))
                .expiredOrders(orderRepository.countByStatusAndPeriod(OrderStatus.EXPIRED, from, to))
                .usedOrders(orderRepository.countByStatusAndPeriod(OrderStatus.USED, from, to))
                .build();
    }

    private ShowTimeInfo buildShowTimeInfo(Orders order) {
        if (order.getOrderTickets() == null || order.getOrderTickets().isEmpty()) return null;
        OrderTickets first = order.getOrderTickets().iterator().next();
        if (first.getSeatShowTime() == null) return null;
        ShowTimes st = first.getSeatShowTime().getShowTimes();
        if (st == null) return null;

        String roomName = null, cinemaName = null, cinemaAddress = null;
        if (st.getRooms() != null) {
            roomName = st.getRooms().getName();
            if (st.getRooms().getCinemas() != null) {
                cinemaName = st.getRooms().getCinemas().getName();
                cinemaAddress = st.getRooms().getCinemas().getAddress();
            }
        }
        return ShowTimeInfo.builder()
                .movieName(st.getMovies() != null ? st.getMovies().getTitle() : null)
                .roomName(roomName)
                .showTime(st.getStartTime())
                .cinemaName(cinemaName)
                .cinemaAddress(cinemaAddress)
                .build();
    }

    private List<OrderTicketResponse> buildTicketResponses(Orders order) {
        if (order.getOrderTickets() == null) return List.of();
        return order.getOrderTickets().stream()
                .filter(t -> t.getSeatShowTime() != null && t.getSeatShowTime().getSeats() != null)
                .map(t -> {
                    Seats seat = t.getSeatShowTime().getSeats();
                    return OrderTicketResponse.builder()
                            .orderTicketId(t.getOrderTicketId())
                            .seatName(seat.getSeatRow() + seat.getSeatNumber())
                            .seatType(seat.getSeatType())
                            .price(t.getPrice())
                            .build();
                })
                .toList();
    }

    private List<OrderFoodResponse> buildFoodResponses(Orders order) {
        if (order.getOrderFoods() == null) return List.of();
        return order.getOrderFoods().stream()
                .map(f -> OrderFoodResponse.builder()
                        .foodId(f.getFoods().getFoodId())
                        .name(f.getFoods().getName())
                        .quantity(f.getQuantity())
                        .unitPrice(f.getUnitPrice())
                        .totalPrice(f.getTotalPrice())
                        .build())
                .toList();
    }

    private AdminOrderSummaryResponse toSummaryResponse(Orders order) {
        String userId = null, fullName = "---";
        if (order.getUsers() != null) {
            userId = order.getUsers().getUserId();
            fullName = order.getUsers().getFirstname() + " " + order.getUsers().getLastname();
        }

        String movieName = null, cinemaName = null;
        LocalDateTime showTime = null;
        int ticketCount = 0;

        if (order.getOrderTickets() != null && !order.getOrderTickets().isEmpty()) {
            ticketCount = order.getOrderTickets().size();
            OrderTickets first = order.getOrderTickets().iterator().next();
            SeatShowTime sst = first.getSeatShowTime();
            if (sst != null && sst.getShowTimes() != null) {
                ShowTimes st = sst.getShowTimes();
                showTime = st.getStartTime();
                if (st.getMovies() != null) movieName = st.getMovies().getTitle();
                if (st.getRooms() != null && st.getRooms().getCinemas() != null)
                    cinemaName = st.getRooms().getCinemas().getName();
            }
        }

        return AdminOrderSummaryResponse.builder()
                .orderId(order.getOrderId())
                .userId(userId)
                .fullName(fullName)
                .movieName(movieName)
                .cinemaName(cinemaName)
                .showTime(showTime)
                .ticketCount(ticketCount)
                .finalPrice(order.getFinalPrice())
                .orderStatus(order.getOrderStatus())
                .bookingTime(order.getBookingTime())
                .build();
    }

}
