package com.example.movie_ticket_be.payment.dto.request;

import com.example.movie_ticket_be.payment.enums.PaymentType;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentConfirmRequest {
    Long orderId;
    String transactionId;
    String paymentInfo;
    PaymentType paymentType;
}
