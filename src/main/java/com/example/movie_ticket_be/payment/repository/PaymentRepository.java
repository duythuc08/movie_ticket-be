package com.example.movie_ticket_be.payment.repository;

import com.example.movie_ticket_be.payment.entity.Payments;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payments,String> {
    Optional<Payments> findByOrder_OrderId(Long orderOrderId);

    Optional<Payments> findByTransactionId(String transactionId);
}
