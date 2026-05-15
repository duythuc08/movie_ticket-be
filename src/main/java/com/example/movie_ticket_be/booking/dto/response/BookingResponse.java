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
public class BookingResponse {
    Long orderId;
    String userId;
    String fullName;
    OrderStatus orderStatus;

    BigDecimal totalTicketPrice;
    BigDecimal totalFoodPrice;
    BigDecimal discountAmount;
    BigDecimal finalPrice;
    String promotionCode;

    LocalDateTime bookingTime;
    LocalDateTime expiredTime;

    List<OrderTicketResponse> tickets;
    List<OrderFoodResponse> foods;

    String paymentUrl;
}
