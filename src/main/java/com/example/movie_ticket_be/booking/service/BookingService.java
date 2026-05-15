package com.example.movie_ticket_be.booking.service;

import com.example.movie_ticket_be.booking.dto.request.BookingRequest;
import com.example.movie_ticket_be.booking.dto.request.OrderFoodsRequest;
import com.example.movie_ticket_be.booking.dto.response.OrderFoodResponse;
import com.example.movie_ticket_be.booking.dto.response.OrderResponse;
import com.example.movie_ticket_be.booking.dto.response.OrderTicketResponse;
import com.example.movie_ticket_be.booking.entity.Foods;
import com.example.movie_ticket_be.booking.entity.OrderFoods;
import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.booking.enums.TicketStatus;
import com.example.movie_ticket_be.booking.repository.FoodRepository;
import com.example.movie_ticket_be.booking.repository.OrderFoodRepository;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.booking.repository.OrderTicketRepository;
import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.promotion.entity.Promotion;
import com.example.movie_ticket_be.promotion.enums.PromotionType;
import com.example.movie_ticket_be.promotion.repository.PromotionRepository;
import com.example.movie_ticket_be.showtime.entity.SeatShowTime;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import com.example.movie_ticket_be.showtime.repository.SeatShowTimeRepository;
import com.example.movie_ticket_be.showtime.service.ShowTimePriceService;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.repository.UserRepository;
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

    private static final int HOLD_SEAT_MINUTES = 5;

    @Transactional(rollbackOn = Exception.class)
    public OrderResponse createBooking(BookingRequest request) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expirationTime = now.plusMinutes(HOLD_SEAT_MINUTES);

        Users user = userRepository.findByUserId(request.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        //LOCK SEAT
        List<SeatShowTime> seats = seatShowTimeRepository.findAllBySeatShowTimeIdIn(request.getSeatShowTimeIds());
        if (seats.size() != request.getSeatShowTimeIds().size()) {
            throw new AppException(ErrorCode.SIZE_MISMATCH);
        }
        for (SeatShowTime seat : seats) {
            boolean isAvailable = false;

            if (seat.getSeatShowTimeStatus() == SeatShowTimeStatus.AVAILABLE) {
                isAvailable = true;
            } else if (seat.getSeatShowTimeStatus() == SeatShowTimeStatus.RESERVED) {
                if (seat.getLockedUntil() != null && seat.getLockedUntil().isBefore(now)) {
                    isAvailable = true;
                }
            }

            if (!isAvailable) {
                throw new RuntimeException("Ghế " + seat.getSeats().getSeatRow() + seat.getSeats().getSeatNumber() + " đã có người đặt hoặc đang được giữ!");
            }

            seat.setUsers(user);
            seat.setSeatShowTimeStatus(SeatShowTimeStatus.RESERVED);
            seat.setLockedUntil(expirationTime);
        }
        seatShowTimeRepository.saveAll(seats);

        //ORDER
        Orders order = Orders.builder()
                .users(user)
                .bookingTime(now)
                .createdAt(now)
                .expiredTime(expirationTime)
                .orderStatus(OrderStatus.PENDING)
                .build();
        orderRepository.save(order);

        //ODER TICKET
        BigDecimal totalTicketPrice = BigDecimal.ZERO;
        List<OrderTickets> tickets = new ArrayList<>();
        Long showTimeId = seats.get(0).getShowTimes().getShowTimeId();
        Map<SeatType, BigDecimal> priceMap = showTimePriceService.getPriceMapByShowTime(showTimeId);

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

        //ODER FOOD
        List<OrderFoods> orderFoods = new ArrayList<>();
        BigDecimal totalFoodPrice = BigDecimal.ZERO;
        if (request.getFoods() != null) {
            for (OrderFoodsRequest foodReq : request.getFoods()) {
                Foods food = foodRepository.findByFoodId(foodReq.getFoodId())
                        .orElseThrow(() -> new AppException(ErrorCode.FOOD_NOT_FOUND));
                BigDecimal totalItem = food.getPrice().multiply(BigDecimal.valueOf(foodReq.getQuantity()));

                OrderFoods item = OrderFoods.builder()
                        .orders(order)
                        .foods(food)
                        .quantity(foodReq.getQuantity())
                        .unitPrice(food.getPrice())
                        .totalPrice(totalItem)
                        .build();
                orderFoods.add(item);
                totalFoodPrice = totalFoodPrice.add(totalItem);
            }
        }
        orderFoodRepository.saveAll(orderFoods);

        //ƯU ĐÃI (HẠNG THÀNH VIÊN & MÃ GIẢM GIÁ)
        BigDecimal provisionalTotal = totalTicketPrice.add(totalFoodPrice);

        BigDecimal memberDiscountAmount = BigDecimal.ZERO;
        if (user.getMembershipTier() != null) {
            BigDecimal applicablePercent = BigDecimal.ZERO;

            if (user.getBirthday() != null && user.getBirthday().getMonth() == now.getMonth()) {
                applicablePercent = user.getMembershipTier().getBirthdayDiscount();
            } else {
                applicablePercent = user.getMembershipTier().getDiscountPercent();
            }

            if (applicablePercent.compareTo(BigDecimal.ZERO) > 0) {
                memberDiscountAmount = provisionalTotal.multiply(applicablePercent)
                        .divide(new BigDecimal(100), RoundingMode.HALF_UP);
            }
        }

        BigDecimal amountAfterMemberDiscount = provisionalTotal.subtract(memberDiscountAmount);

        //MÃ GIẢM GIÁ
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
                if (promotion.getMaxDiscountAmount() != null && promotionDiscount.compareTo(promotion.getMaxDiscountAmount()) > 0) {
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

        //CẬP NHẬT VÀ LƯU ORDER CUỐI CÙNG
        BigDecimal finalPrice = amountAfterMemberDiscount.subtract(promotionDiscount);
        if (finalPrice.compareTo(BigDecimal.ZERO) < 0) finalPrice = BigDecimal.ZERO;

        // --- Tính điểm tích lũyd
        int pointsEarned = finalPrice.divide(new BigDecimal(1000), 0, RoundingMode.FLOOR).intValue();

        order.setTotalFoodPrice(totalFoodPrice);
        order.setTotalTicketPrice(totalTicketPrice);
        order.setMemberDiscountAmount(memberDiscountAmount);
        order.setDiscountAmount(promotionDiscount);
        order.setPromotionCode(appliedPromotionCode);
        order.setFinalPrice(finalPrice);
        order.setPointsEarned(pointsEarned);
        order.setUpdatedAt(now);

        Orders savedOrder = orderRepository.save(order);

        // 8.RESPONSE
        List<OrderTicketResponse> ticketResponses = tickets.stream()
                .map(ticket -> OrderTicketResponse.builder()
                        .orderTicketId(ticket.getOrderTicketId())
                        .seatName(ticket.getSeatShowTime().getSeats().getSeatRow() + ticket.getSeatShowTime().getSeats().getSeatNumber())
                        .price(ticket.getPrice())
                        .seatType(ticket.getSeatShowTime().getSeats().getSeatType())
                        .build())
                .toList();

        List<OrderFoodResponse> foodResponses = orderFoods.stream()
                .map(food -> OrderFoodResponse.builder()
                        .foodId(food.getFoods().getFoodId())
                        .name(food.getFoods().getName())
                        .quantity(food.getQuantity())
                        .unitPrice(food.getUnitPrice())
                        .totalPrice(food.getTotalPrice())
                        .build())
                .toList();

        return OrderResponse.builder()
                .orderId(savedOrder.getOrderId())
                .userId(savedOrder.getUsers().getUserId())
                .fullName(savedOrder.getUsers().getFirstname() + " " + savedOrder.getUsers().getLastname())
                .totalTicketPrice(savedOrder.getTotalTicketPrice())
                .totalFoodPrice(savedOrder.getTotalFoodPrice())
                .memberDiscountAmount(savedOrder.getMemberDiscountAmount())
                .discountAmount(savedOrder.getDiscountAmount())
                .promotionCode(savedOrder.getPromotionCode())
                .finalPrice(savedOrder.getFinalPrice())
                .pointsEarned(savedOrder.getPointsEarned())
                .bookingTime(savedOrder.getBookingTime())
                .expiredTime(savedOrder.getExpiredTime())
                .createdAt(savedOrder.getCreatedAt())
                .updatedAt(savedOrder.getUpdatedAt())
                .orderStatus(savedOrder.getOrderStatus())
                .tickets(ticketResponses)
                .foods(foodResponses)
                .qrCode(savedOrder.getQrCode())
                .build();
    }
}