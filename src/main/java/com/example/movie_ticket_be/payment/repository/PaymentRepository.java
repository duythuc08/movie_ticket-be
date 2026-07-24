package com.example.movie_ticket_be.payment.repository;

import com.example.movie_ticket_be.payment.entity.Payments;
import com.example.movie_ticket_be.payment.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments, Long> {
	Optional<Payments> findByOrder_OrderId(Long orderOrderId);

	Optional<Payments> findByTransactionId(String transactionId);

	java.util.List<Payments> findByOrder_OrderIdAndPaymentStatus(Long orderId, PaymentStatus paymentStatus);
}
