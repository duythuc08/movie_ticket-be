package com.example.movie_ticket_be.booking.service;

import com.example.movie_ticket_be.booking.dto.request.AddFoodsRequest;
import com.example.movie_ticket_be.booking.dto.request.CheckoutRequest;
import com.example.movie_ticket_be.booking.dto.request.InitiateBookingRequest;
import com.example.movie_ticket_be.booking.dto.request.OrderFoodsRequest;
import com.example.movie_ticket_be.booking.dto.response.*;
import com.example.movie_ticket_be.cinema.entity.Foods;
import com.example.movie_ticket_be.cinema.enums.FoodStatus;
import com.example.movie_ticket_be.cinema.repository.FoodRepository;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.booking.entity.OrderFoods;
import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.booking.enums.TicketStatus;
import com.example.movie_ticket_be.booking.repository.OrderFoodRepository;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.booking.repository.OrderTicketRepository;
import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.payment.enums.PaymentType;
import com.example.movie_ticket_be.payment.service.PaymentService;
import com.example.movie_ticket_be.payment.service.VNPayService;
import com.example.movie_ticket_be.promotion.entity.Promotion;
import com.example.movie_ticket_be.promotion.enums.PromotionType;
import com.example.movie_ticket_be.promotion.repository.PromotionRepository;
import com.example.movie_ticket_be.showtime.entity.SeatShowTime;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import com.example.movie_ticket_be.showtime.repository.SeatShowTimeRepository;
import com.example.movie_ticket_be.showtime.service.ShowTimePriceService;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingService {
	OrderRepository orderRepository;
	OrderTicketRepository orderTicketRepository;
	OrderFoodRepository orderFoodRepository;
	SeatShowTimeRepository seatShowTimeRepository;
	FoodRepository foodRepository;
	UserRepository userRepository;
	ShowTimePriceService showTimePriceService;
	PromotionRepository promotionRepository;
	PaymentService paymentService;
	VNPayService vnPayService;

	private static final int HOLD_SEAT_MINUTES = 5;
	private static final int PAYMENT_WINDOW_MINUTES = 10;

	// ──────────────────────────────────────────────────────────────────────────
	// ENDPOINT 1: Khóa ghế + tạo Order + OrderTickets
	// ──────────────────────────────────────────────────────────────────────────
	@Transactional(rollbackOn = Exception.class)
	public InitiateBookingResponse initiateBooking(InitiateBookingRequest request) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime expirationTime = now.plusMinutes(HOLD_SEAT_MINUTES);

		Users user = userRepository.findByUserId(request.getUserId())
				.orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

		List<Long> ids = request.getSeatShowTimeIds();
		if (ids == null || ids.isEmpty()) {
			throw new AppException(ErrorCode.INVALID_SEAT_SELECTION);
		}

		// Atomic UPDATE — trả về số dòng bị ảnh hưởng
		int affected = seatShowTimeRepository.atomicReserveSeats(ids, user, expirationTime, now);
		if (affected != ids.size()) {
			throw new AppException(ErrorCode.SEAT_ALREADY_RESERVED);
		}

		// Tạo Order
		Orders order = Orders.builder()
				.users(user)
				.bookingTime(now)
				.createdAt(now)
				.expiredTime(expirationTime)
				.orderStatus(OrderStatus.PENDING)
				.totalTicketPrice(BigDecimal.ZERO)
				.totalFoodPrice(BigDecimal.ZERO)
				.memberDiscountAmount(BigDecimal.ZERO)
				.discountAmount(BigDecimal.ZERO)
				.finalPrice(BigDecimal.ZERO)
				.build();
		orderRepository.save(order);

		// Tạo OrderTickets
		List<SeatShowTime> seats = seatShowTimeRepository.findAllBySeatShowTimeIdIn(ids);
		Long showTimeId = seats.get(0).getShowTimes().getShowTimeId();
		Map<SeatType, BigDecimal> priceMap = showTimePriceService.getPriceMapByShowTime(showTimeId);

		BigDecimal totalTicketPrice = BigDecimal.ZERO;
		List<OrderTickets> tickets = new ArrayList<>();
		for (SeatShowTime seat : seats) {
			SeatType currentSeatType = seat.getSeats().getSeatType();
			BigDecimal price = priceMap.get(currentSeatType);
			if (price == null) {
				throw new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND);
			}
			OrderTickets ticket = OrderTickets.builder()
					.orders(order)
					.seatShowTime(seat)
					.price(price)
					.ticketStatus(TicketStatus.RESERVED)
					.createdAt(now)
					.build();
			tickets.add(ticket);
			totalTicketPrice = totalTicketPrice.add(price);
		}
		orderTicketRepository.saveAll(tickets);

		order.setTotalTicketPrice(totalTicketPrice);
		order.setFinalPrice(totalTicketPrice);
		order.setUpdatedAt(now);
		orderRepository.save(order);

		// Build showTimeInfo
		ShowTimes showTimes = seats.get(0).getShowTimes();
		ShowTimeInfo showTimeInfo = buildShowTimeInfo(showTimes);

		List<OrderTicketResponse> ticketResponses = tickets.stream()
				.map(t -> OrderTicketResponse.builder()
						.orderTicketId(t.getOrderTicketId())
						.seatName(t.getSeatShowTime().getSeats().getSeatRow()
								+ t.getSeatShowTime().getSeats().getSeatNumber())
						.price(t.getPrice())
						.seatType(t.getSeatShowTime().getSeats().getSeatType())
						.build())
				.toList();

		return InitiateBookingResponse.builder()
				.orderId(order.getOrderId())
				.totalTicketPrice(totalTicketPrice)
				.expiredTime(expirationTime)
				.bookingTime(now)
				.showTimeInfo(showTimeInfo)
				.tickets(ticketResponses)
				.build();
	}

	// ──────────────────────────────────────────────────────────────────────────
	// ENDPOINT NGOẠI LỆ: Thả ghế khi user bấm Back
	// ──────────────────────────────────────────────────────────────────────────
	@Transactional(rollbackOn = Exception.class)
	public void releaseBooking(Long orderId) {
		Orders order = orderRepository.findByOrderId(orderId)
				.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
		if (order.getOrderStatus() != OrderStatus.PENDING) {
			throw new AppException(ErrorCode.ORDER_NOT_PENDING);
		}

		// Thả ghế về AVAILABLE
		List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(orderId);
		for (OrderTickets t : tickets) {
			SeatShowTime sst = t.getSeatShowTime();
			sst.setSeatShowTimeStatus(SeatShowTimeStatus.AVAILABLE);
			sst.setUsers(null);
			sst.setLockedUntil(null);
			seatShowTimeRepository.save(sst);
		}

		// Xóa OrderTickets
		orderTicketRepository.deleteAll(tickets);

		// Huỷ Order
		order.setOrderStatus(OrderStatus.CANCELLED);
		order.setUpdatedAt(LocalDateTime.now());
		orderRepository.save(order);
	}

	// ──────────────────────────────────────────────────────────────────────────
	// ENDPOINT 2: Thêm Food & Drink vào đơn hàng
	// ──────────────────────────────────────────────────────────────────────────
	@Transactional(rollbackOn = Exception.class)
	public OrderResponse addFoods(Long orderId, AddFoodsRequest request) {
		Orders order = orderRepository.findByOrderId(orderId)
				.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
		if (order.getOrderStatus() != OrderStatus.PENDING) {
			throw new AppException(ErrorCode.ORDER_NOT_PENDING);
		}

		// Xóa food cũ nếu có (để idempotent khi gọi lại)
		List<OrderFoods> existingFoods = orderFoodRepository.findByOrders_OrderId(orderId);
		if (!existingFoods.isEmpty()) {
			// Hoàn tồn kho trước khi ghi đè
			for (OrderFoods of : existingFoods) {
				Foods food = of.getFoods();
				food.setStockQuantity(food.getStockQuantity() + of.getQuantity());
				if (food.getFoodStatus() == FoodStatus.OUT_OF_STOCK) {
					food.setFoodStatus(FoodStatus.IN_STOCK);
					food.setEntityStatus(EntityStatus.ACTIVE);
				}
				foodRepository.save(food);
			}
			orderFoodRepository.deleteAll(existingFoods);
		}

		// Lấy cinemaId từ showtime
		List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(orderId);
		if (tickets.isEmpty()) {
			throw new AppException(ErrorCode.ORDER_NOT_FOUND);
		}
		Long cinemaId = tickets.get(0).getSeatShowTime().getShowTimes().getRooms().getCinemas().getCinemaId();

		List<OrderFoods> orderFoods = new ArrayList<>();
		List<Foods> updatedFoods = new ArrayList<>();
		BigDecimal totalFoodPrice = BigDecimal.ZERO;

		if (request.getFoods() != null) {
			for (OrderFoodsRequest foodReq : request.getFoods()) {
				Foods food = foodRepository.findByFoodId(foodReq.getFoodId())
						.orElseThrow(() -> new AppException(ErrorCode.FOOD_NOT_FOUND));
				if (!food.getCinema().getCinemaId().equals(cinemaId)) {
					throw new AppException(ErrorCode.FOOD_NOT_BELONG_TO_CINEMA);
				}
				if (food.getStockQuantity() < foodReq.getQuantity()) {
					throw new AppException(ErrorCode.FOOD_OUT_OF_STOCK);
				}

				int newStock = food.getStockQuantity() - foodReq.getQuantity();
				food.setStockQuantity(newStock);
				if (newStock == 0) {
					food.setFoodStatus(FoodStatus.OUT_OF_STOCK);
					food.setEntityStatus(EntityStatus.INACTIVE);
				}
				updatedFoods.add(food);

				BigDecimal totalItem = food.getPrice().multiply(BigDecimal.valueOf(foodReq.getQuantity()));
				orderFoods.add(OrderFoods.builder()
						.orders(order)
						.foods(food)
						.quantity(foodReq.getQuantity())
						.unitPrice(food.getPrice())
						.totalPrice(totalItem)
						.build());
				totalFoodPrice = totalFoodPrice.add(totalItem);
			}
		}

		foodRepository.saveAll(updatedFoods);
		orderFoodRepository.saveAll(orderFoods);

		LocalDateTime now = LocalDateTime.now();
		order.setTotalFoodPrice(totalFoodPrice);
		order.setFinalPrice(order.getTotalTicketPrice().add(totalFoodPrice));
		order.setUpdatedAt(now);
		orderRepository.save(order);

		return buildOrderResponse(order, tickets, orderFoods);
	}

	// ──────────────────────────────────────────────────────────────────────────
	// ENDPOINT 3: Checkout — áp dụng giảm giá, đóng băng đơn, trả VNPay URL
	// ──────────────────────────────────────────────────────────────────────────
	@Transactional(rollbackOn = Exception.class)
	public CheckoutResponse checkout(Long orderId, CheckoutRequest request, HttpServletRequest httpRequest) {
		LocalDateTime now = LocalDateTime.now();
		Orders order = orderRepository.findByOrderId(orderId)
				.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
		if (order.getOrderStatus() != OrderStatus.PENDING) {
			throw new AppException(ErrorCode.ORDER_NOT_PENDING);
		}

		Users user = order.getUsers();
		BigDecimal provisionalTotal = order.getTotalTicketPrice().add(order.getTotalFoodPrice());

		// Giảm giá thành viên
		BigDecimal memberDiscountAmount = BigDecimal.ZERO;
		if (user.getMembershipTier() != null) {
			BigDecimal applicablePercent = BigDecimal.ZERO;
			if (user.getBirthday() != null && user.getBirthday().getMonth() == now.getMonth()) {
				applicablePercent = user.getMembershipTier().getBirthdayDiscount();
			} else {
				applicablePercent = user.getMembershipTier().getDiscountPercent();
			}
			if (applicablePercent.compareTo(BigDecimal.ZERO) > 0) {
				memberDiscountAmount = provisionalTotal
						.multiply(applicablePercent)
						.divide(new BigDecimal(100), RoundingMode.HALF_UP);
			}
		}

		BigDecimal amountAfterMemberDiscount = provisionalTotal.subtract(memberDiscountAmount);

		// Mã giảm giá
		BigDecimal promotionDiscount = BigDecimal.ZERO;
		String appliedPromotionCode = null;
		if (request.getPromotionCode() != null && !request.getPromotionCode().trim().isEmpty()) {
			Promotion promotion = promotionRepository.findByCode(request.getPromotionCode())
					.orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

			if (promotion.getEndTime().isBefore(now) || promotion.getStartTime().isAfter(now)) {
				throw new AppException(ErrorCode.PROMOTION_EXPIRED);
			}
			if (promotion.getUseLimit() <= 0) {
				throw new AppException(ErrorCode.PROMOTION_OUT_OF_STOCK);
			}
			if (amountAfterMemberDiscount.compareTo(promotion.getMinOrderValue()) < 0) {
				throw new AppException(ErrorCode.PROMOTION_CONDITION_NOT_MET);
			}

			if (promotion.getType().equals(PromotionType.PERCENTAGE)) {
				BigDecimal percentage = promotion.getDiscountValue().divide(new BigDecimal(100));
				promotionDiscount = amountAfterMemberDiscount.multiply(percentage);
				if (promotion.getMaxDiscountAmount() != null
						&& promotionDiscount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
					promotionDiscount = promotion.getMaxDiscountAmount();
				}
			} else if (promotion.getType().equals(PromotionType.FIXED_AMOUNT)) {
				promotionDiscount = promotion.getDiscountValue();
			}

			if (promotionDiscount.compareTo(amountAfterMemberDiscount) > 0) {
				promotionDiscount = amountAfterMemberDiscount;
			}

			promotion.setUseLimit(promotion.getUseLimit() - 1);
			promotionRepository.save(promotion);
			appliedPromotionCode = promotion.getCode();
		}

		BigDecimal finalPrice = amountAfterMemberDiscount.subtract(promotionDiscount);
		if (finalPrice.compareTo(BigDecimal.ZERO) < 0) finalPrice = BigDecimal.ZERO;

		int pointsEarned = finalPrice.divide(new BigDecimal(1000), 0, RoundingMode.FLOOR).intValue();

		// Đóng băng đơn → IN_PROGRESS, extend expiredTime thêm 10 phút
		order.setMemberDiscountAmount(memberDiscountAmount);
		order.setDiscountAmount(promotionDiscount);
		order.setPromotionCode(appliedPromotionCode);
		order.setFinalPrice(finalPrice);
		order.setPointsEarned(pointsEarned);
		order.setOrderStatus(OrderStatus.IN_PROGRESS);
		order.setExpiredTime(now.plusMinutes(PAYMENT_WINDOW_MINUTES));
		order.setUpdatedAt(now);
		orderRepository.save(order);

		// Tạo Payment PENDING
		paymentService.createPendingPayment(orderId, PaymentType.VNPAY);

		// Tạo VNPay URL
		String paymentUrl = vnPayService.createPaymentUrl(httpRequest, orderId, finalPrice);

		return CheckoutResponse.builder()
				.orderId(orderId)
				.paymentUrl(paymentUrl)
				.totalTicketPrice(order.getTotalTicketPrice())
				.totalFoodPrice(order.getTotalFoodPrice())
				.memberDiscountAmount(memberDiscountAmount)
				.discountAmount(promotionDiscount)
				.finalPrice(finalPrice)
				.build();
	}

	// ──────────────────────────────────────────────────────────────────────────
	// Helpers
	// ──────────────────────────────────────────────────────────────────────────
	private ShowTimeInfo buildShowTimeInfo(ShowTimes showTimes) {
		String roomName = null, cinemaName = null, cinemaAddress = null;
		if (showTimes.getRooms() != null) {
			roomName = showTimes.getRooms().getName();
			if (showTimes.getRooms().getCinemas() != null) {
				cinemaName = showTimes.getRooms().getCinemas().getName();
				cinemaAddress = showTimes.getRooms().getCinemas().getAddress();
			}
		}
		return ShowTimeInfo.builder()
				.movieName(showTimes.getMovies() != null ? showTimes.getMovies().getTitle() : null)
				.roomName(roomName)
				.showTime(showTimes.getStartTime())
				.cinemaName(cinemaName)
				.cinemaAddress(cinemaAddress)
				.build();
	}

	private OrderResponse buildOrderResponse(Orders order, List<OrderTickets> tickets, List<OrderFoods> orderFoods) {
		ShowTimes showTimes = tickets.get(0).getSeatShowTime().getShowTimes();

		List<OrderTicketResponse> ticketResponses = tickets.stream()
				.map(t -> OrderTicketResponse.builder()
						.orderTicketId(t.getOrderTicketId())
						.seatName(t.getSeatShowTime().getSeats().getSeatRow()
								+ t.getSeatShowTime().getSeats().getSeatNumber())
						.price(t.getPrice())
						.seatType(t.getSeatShowTime().getSeats().getSeatType())
						.build())
				.toList();

		List<OrderFoodResponse> foodResponses = orderFoods.stream()
				.map(f -> OrderFoodResponse.builder()
						.foodId(f.getFoods().getFoodId())
						.name(f.getFoods().getName())
						.quantity(f.getQuantity())
						.unitPrice(f.getUnitPrice())
						.totalPrice(f.getTotalPrice())
						.build())
				.toList();

		return OrderResponse.builder()
				.orderId(order.getOrderId())
				.userId(order.getUsers().getUserId())
				.fullName(order.getUsers().getFirstname() + " " + order.getUsers().getLastname())
				.showTimeInfo(buildShowTimeInfo(showTimes))
				.totalTicketPrice(order.getTotalTicketPrice())
				.totalFoodPrice(order.getTotalFoodPrice())
				.memberDiscountAmount(order.getMemberDiscountAmount())
				.discountAmount(order.getDiscountAmount())
				.promotionCode(order.getPromotionCode())
				.finalPrice(order.getFinalPrice())
				.pointsEarned(order.getPointsEarned())
				.bookingTime(order.getBookingTime())
				.expiredTime(order.getExpiredTime())
				.createdAt(order.getCreatedAt())
				.updatedAt(order.getUpdatedAt())
				.orderStatus(order.getOrderStatus())
				.tickets(ticketResponses)
				.foods(foodResponses)
				.qrCode(order.getQrCode())
				.build();
	}
}
