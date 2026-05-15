package com.example.movie_ticket_be.booking.dto.response;


import com.example.movie_ticket_be.booking.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderResponse {
    Long orderId;
    String userId;
    String fullName;

    BigDecimal totalTicketPrice;
    BigDecimal totalFoodPrice;
    BigDecimal discountAmount;
    BigDecimal finalPrice;
    String promotionCode;
    OrderStatus orderStatus;
    LocalDateTime bookingTime;
    LocalDateTime expiredTime;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    String qrCode;

    List<OrderTicketResponse> tickets;
    List<OrderFoodResponse> foods;

    BigDecimal memberDiscountAmount;
    int pointsEarned;

    String movieTitle;
    String cinemaName;
    String cinemaAddress;
    String showTime;
    String roomName;
}
