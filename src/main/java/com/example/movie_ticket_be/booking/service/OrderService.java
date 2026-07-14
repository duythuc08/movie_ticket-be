package com.example.movie_ticket_be.booking.service;

import com.example.movie_ticket_be.booking.dto.response.*;
import com.example.movie_ticket_be.booking.entity.OrderFoods;
import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.booking.mapper.OrderMapper;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.payment.repository.PaymentRepository;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderService {

	OrderRepository orderRepository;
	OrderMapper orderMapper;
	PaymentRepository paymentRepository;

	@PreAuthorize("isAuthenticated()")
	public OrderResponse getOrderById(Long orderId) {
		try {
			Orders order = orderRepository.findByOrderId(orderId)
					.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

			String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
			if (!orderRepository.existsByOrderIdAndUsers_Username(orderId, currentUsername)) {
				throw new AppException(ErrorCode.UNAUTHORIZED);
			}

			String userId = null;
			String fullName = "---";
			if (order.getUsers() != null) {
				userId = order.getUsers().getUserId();
				fullName = order.getUsers().getFirstname() + " " + order.getUsers().getLastname();
			}

			// LIST TICKET
			List<OrderTicketResponse> ticketResponses = new ArrayList<>();
			if (order.getOrderTickets() != null) {
				for (OrderTickets ticket : order.getOrderTickets()) {
					if (ticket.getSeatShowTime() != null && ticket.getSeatShowTime().getSeats() != null) {
						var seat = ticket.getSeatShowTime().getSeats();
						ticketResponses.add(OrderTicketResponse.builder().orderTicketId(ticket.getOrderTicketId())
								.seatName(seat.getSeatRow() + seat.getSeatNumber()).seatType(seat.getSeatType())
								.price(ticket.getPrice()).build());
					}
				}
			}

			// LIST FOOD
			List<OrderFoodResponse> foodResponses = new ArrayList<>();
			if (order.getOrderFoods() != null) {
				for (OrderFoods food : order.getOrderFoods()) {
					foodResponses.add(OrderFoodResponse.builder().foodId(food.getFoods().getFoodId())
							.name(food.getFoods().getName()).quantity(food.getQuantity()).unitPrice(food.getUnitPrice())
							.totalPrice(food.getTotalPrice()).build());
				}
			}

			// SHOWTIME INFO
			ShowTimeInfo showTimeInfo = null;
			if (order.getOrderTickets() != null && !order.getOrderTickets().isEmpty()) {
				OrderTickets first = order.getOrderTickets().iterator().next();
				if (first.getSeatShowTime() != null) {
					ShowTimes st = first.getSeatShowTime().getShowTimes();
					if (st != null) {
						String roomName = null, cinemaName = null, cinemaAddress = null;
						if (st.getRooms() != null) {
							roomName = st.getRooms().getName();
							if (st.getRooms().getCinemas() != null) {
								cinemaName = st.getRooms().getCinemas().getName();
								cinemaAddress = st.getRooms().getCinemas().getAddress();
							}
						}
						showTimeInfo = ShowTimeInfo.builder()
								.movieName(st.getMovies() != null ? st.getMovies().getTitle() : null).roomName(roomName)
								.showTime(st.getStartTime()).cinemaName(cinemaName).cinemaAddress(cinemaAddress)
								.build();
					}
				}
			}

			// PAYMENT
			PaymentResponse paymentResponse = paymentRepository.findByOrder_OrderId(orderId)
					.map(p -> PaymentResponse.builder().paymentId(p.getPaymentId()).amount(p.getAmount())
							.transactionId(p.getTransactionId()).paymentInfo(p.getPaymentInfo())
							.paymentDate(p.getPaymentDate()).paymentType(p.getPaymentType())
							.paymentStatus(p.getPaymentStatus()).build())
					.orElse(null);

			return OrderResponse.builder().orderId(order.getOrderId()).userId(userId).fullName(fullName)
					.showTimeInfo(showTimeInfo).totalTicketPrice(order.getTotalTicketPrice())
					.totalFoodPrice(order.getTotalFoodPrice()).memberDiscountAmount(order.getMemberDiscountAmount())
					.discountAmount(order.getDiscountAmount()).pointsEarned(order.getPointsEarned())
					.finalPrice(order.getFinalPrice()).promotionCode(order.getPromotionCode())
					.orderStatus(order.getOrderStatus()).bookingTime(order.getBookingTime())
					.expiredTime(order.getExpiredTime()).createdAt(order.getCreatedAt()).updatedAt(order.getUpdatedAt())
					.qrCode(order.getQrCode()).tickets(ticketResponses).foods(foodResponses).payment(paymentResponse)
					.build();

		} catch (AppException e) {
			log.error("AppException: {}", e.getMessage());
			throw e;
		} catch (Exception e) {
			throw new RuntimeException("Error getting order: " + e.getMessage(), e);
		}
	}

	@PreAuthorize("isAuthenticated()")
	public List<OrderResponse> getOrdersByUserId(String userId) {
		String currentUsername = SecurityContextHolder.getContext().getAuthentication().getName();
		if (!orderRepository.existsByUsers_UserIdAndUsers_Username(userId, currentUsername)) {
			throw new AppException(ErrorCode.UNAUTHORIZED);
		}
		return orderRepository.findByUsers_UserIdAndOrderStatus(userId, OrderStatus.PAID).stream()
				.map(orderMapper::toOrderResponse).toList();
	}
}
