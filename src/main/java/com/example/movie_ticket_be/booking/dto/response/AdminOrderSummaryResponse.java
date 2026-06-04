package com.example.movie_ticket_be.booking.dto.response;

import com.example.movie_ticket_be.booking.enums.OrderStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminOrderSummaryResponse {
	Long orderId;
	String userId;
	String fullName;
	String movieName;
	String cinemaName;
	LocalDateTime showTime;
	int ticketCount;
	BigDecimal finalPrice;
	OrderStatus orderStatus;
	LocalDateTime bookingTime;
}
