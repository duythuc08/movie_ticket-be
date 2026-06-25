package com.example.movie_ticket_be.booking.dto.response;

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
public class InitiateBookingResponse {
	Long orderId;
	BigDecimal totalTicketPrice;
	LocalDateTime expiredTime;
	LocalDateTime bookingTime;
	ShowTimeInfo showTimeInfo;
	List<OrderTicketResponse> tickets;
}
