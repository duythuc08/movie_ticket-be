package com.example.movie_ticket_be.payment.entity;

import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.payment.enums.PaymentStatus;
import com.example.movie_ticket_be.payment.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Payments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long paymentId;

    BigDecimal amount;

    String transactionId;

    String paymentInfo;

    LocalDateTime paymentDate;

    @Enumerated(EnumType.STRING)
    PaymentType paymentType;

    @Enumerated(EnumType.STRING)
    PaymentStatus paymentStatus;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    Orders order;
}
