package com.example.movie_ticket_be.booking.dto.response;

import com.example.movie_ticket_be.payment.enums.PaymentStatus;
import com.example.movie_ticket_be.payment.enums.PaymentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentResponse {
	Long paymentId;
	BigDecimal amount;
	String transactionId;
	String paymentInfo;
	LocalDateTime paymentDate;
	PaymentType paymentType;
	PaymentStatus paymentStatus;
}
