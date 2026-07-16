
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
	MomoService momoService;

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
	public void deletePendingPayment(Long orderId) {
		paymentRepository.findByOrder_OrderIdAndPaymentStatus(orderId, PaymentStatus.PENDING)
				.ifPresent(paymentRepository::delete);
	}

	public Payments getExistingPaymentType(Long orderId) {
		return paymentRepository.findByOrder_OrderId(orderId).orElse(null);
	}

	@Transactional
	public void processSuccess(PaymentConfirmRequest request) {
		// Idempotency guard: cùng transactionId đã xử lý thành công → bỏ qua
		if (request.getTransactionId() != null
				&& paymentRepository.findByTransactionId(request.getTransactionId()).isPresent()) {
			return;
		}

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

		order.setOrderStatus(OrderStatus.PAID);
		
		// Nâng cấp Data QR Code thành JSON
		String movieName = order.getOrderTickets().stream().findFirst().map(t -> t.getSeatShowTime().getShowTimes().getMovies().getTitle()).orElse("N/A");
		String cinemaName = order.getOrderTickets().stream().findFirst().map(t -> t.getSeatShowTime().getShowTimes().getRooms().getCinemas().getName()).orElse("N/A");
		String seats = order.getOrderTickets().stream().map(t -> t.getSeatShowTime().getSeats().getSeatRow() + t.getSeatShowTime().getSeats().getSeatNumber()).collect(java.util.stream.Collectors.joining(","));
		String showTime = order.getOrderTickets().stream().findFirst().map(t -> {
			java.time.LocalDateTime st = t.getSeatShowTime().getShowTimes().getStartTime();
			return st.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy"));
		}).orElse("N/A");
		
		movieName = movieName.length() > 50 ? movieName.substring(0, 50) : movieName;
		cinemaName = cinemaName.length() > 50 ? cinemaName.substring(0, 50) : cinemaName;
		seats = seats.length() > 50 ? seats.substring(0, 50) + "..." : seats;
		
		String bookingCode = order.getQrCode();
		String qrCode = String.format("{\"id\":%d,\"code\":\"%s\",\"movie\":\"%s\",\"cinema\":\"%s\",\"time\":\"%s\",\"seats\":\"%s\"}",
				order.getOrderId(),
				bookingCode,
				movieName.replace("\"", "\\\""),
				cinemaName.replace("\"", "\\\""),
				showTime,
				seats);

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
			sendPaymentSuccessMail(order.getUsers(), order, bookingCode, qrBase64);
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
	@Transactional
	public String retryMomoPayment(Long orderId) {
		Orders order = orderRepository.findByOrderId(orderId)
				.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

		if (order.getOrderStatus() != OrderStatus.CANCELLED) {
			throw new AppException(ErrorCode.ORDER_NOT_FOUND);
		}

		List<OrderTickets> tickets = orderTicketRepository.findByOrders_OrderId(orderId);
		for (OrderTickets ticket : tickets) {
			if (ticket.getSeatShowTime().getSeatShowTimeStatus() != SeatShowTimeStatus.AVAILABLE) {
				throw new AppException(ErrorCode.SEAT_ALREADY_BOOKED);
			}
		}

		List<OrderFoods> orderFoods = orderFoodRepository.findByOrders_OrderId(orderId);
		for (OrderFoods of : orderFoods) {
			if (of.getFoods().getStockQuantity() < of.getQuantity()) {
				throw new AppException(ErrorCode.FOOD_OUT_OF_STOCK);
			}
		}

		LocalDateTime expiredTime = LocalDateTime.now().plusMinutes(10);

		for (OrderTickets ticket : tickets) {
			ticket.setTicketStatus(TicketStatus.RESERVED);
			SeatShowTime sst = ticket.getSeatShowTime();
			sst.setSeatShowTimeStatus(SeatShowTimeStatus.RESERVED);
			sst.setLockedUntil(expiredTime);
			seatShowTimeRepository.save(sst);
		}
		orderTicketRepository.saveAll(tickets);

		for (OrderFoods of : orderFoods) {
			Foods food = of.getFoods();
			food.setStockQuantity(food.getStockQuantity() - of.getQuantity());
			if (food.getStockQuantity() <= 0) food.setFoodStatus(FoodStatus.OUT_OF_STOCK);
		}
		foodRepository.saveAll(orderFoods.stream().map(OrderFoods::getFoods).toList());

		if (order.getPromotionCode() != null) {
			promotionRepository.findByCodeIgnoreCase(order.getPromotionCode())
					.ifPresent(p -> {
						if (p.getUseLimit() != null) p.setUseLimit(p.getUseLimit() - 1);
						promotionRepository.save(p);
					});
		}

		order.setOrderStatus(OrderStatus.IN_PROGRESS);
		order.setExpiredTime(expiredTime);
		orderRepository.save(order);

		createPendingPayment(orderId, PaymentType.MOMO);

		return momoService.createPaymentUrl(orderId, order.getFinalPrice());
	}

	private void sendPaymentSuccessMail(Users user, Orders order, String bookingCode, String qrBase64) {
		String movieName = order.getOrderTickets().stream().findFirst().map(t -> t.getSeatShowTime().getShowTimes().getMovies().getTitle()).orElse("---");
		String code = bookingCode != null ? bookingCode : "---";
		String cinemaName = order.getOrderTickets().stream().findFirst().map(t -> t.getSeatShowTime().getShowTimes().getRooms().getCinemas().getName()).orElse("---");
		String showTime = order.getOrderTickets().stream().findFirst().map(t -> {
			java.time.LocalDateTime st = t.getSeatShowTime().getShowTimes().getStartTime();
			return st.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy"));
		}).orElse("---");
		String seats = order.getOrderTickets().stream().map(t -> t.getSeatShowTime().getSeats().getSeatRow() + t.getSeatShowTime().getSeats().getSeatNumber()).collect(java.util.stream.Collectors.joining(", "));

		String htmlMessage = "<html>"
				+ "<body style=\"font-family: Arial, sans-serif; background-color: #f5f5f5; padding: 20px;\">"
				+ "<div style=\"max-width: 600px; margin: auto; background-color: #fff; padding: 20px; border-radius: 12px; "
				+ "box-shadow: 0 4px 15px rgba(0,0,0,0.1);\">"
				+ "<div style=\"text-align: center; border-bottom: 2px dashed #eee; padding-bottom: 20px; margin-bottom: 20px;\">"
				+ "<h2 style=\"color: #007bff; margin: 0;\">Thanh Toán Thành Công 🎉</h2>"
				+ "<p style=\"color: #666; margin-top: 10px;\">Mã đặt vé: <b>" + code + "</b></p>"
				+ "</div>"
				+ "<p>Xin chào <b>" + user.getFirstname() + " " + user.getLastname() + "</b>,</p>"
				+ "<p>Cảm ơn bạn đã lựa chọn <b>Infinity Cinema</b>. Dưới đây là thông tin vé của bạn:</p>"
				+ "<div style=\"background-color: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0;\">"
				+ "<p style=\"margin: 5px 0;\">🎬 <b>Phim:</b> <span style=\"color: #007bff; font-weight: bold;\">" + movieName + "</span></p>"
				+ "<p style=\"margin: 5px 0;\">📍 <b>Rạp:</b> " + cinemaName + "</p>"
				+ "<p style=\"margin: 5px 0;\">⏰ <b>Suất chiếu:</b> " + showTime + "</p>"
				+ "<p style=\"margin: 5px 0;\">🎟 <b>Ghế ngồi:</b> " + seats + "</p>"
				+ "<p style=\"margin: 5px 0;\">💰 <b>Tổng tiền:</b> " + order.getFinalPrice() + " VND</p>"
				+ "</div>"
				+ "<p style=\"text-align: center; margin-top: 30px; font-weight: bold;\">MÃ QR QUÉT VÉ</p>"
				+ "<div style=\"text-align: center; margin: 10px;\">"
				+ "<img src=\"cid:qrCodeImage\" alt=\"QR Code\" style=\"width:250px;height:250px; border: 4px solid #f8f9fa; border-radius: 12px;\"/>"
				+ "</div>"
				+ "<p style=\"text-align: center; font-size: 14px; color: #555;\">Vui lòng đưa mã QR này cho nhân viên soát vé khi tới rạp.</p>"
				+ "<p style=\"text-align: center; font-size: 14px; color: #555;\">Chúc bạn xem phim vui vẻ!</p>"
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
