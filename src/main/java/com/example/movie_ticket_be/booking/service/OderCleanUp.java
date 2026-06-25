package com.example.movie_ticket_be.booking.service;

import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.booking.repository.OrderRepository;
import com.example.movie_ticket_be.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OderCleanUp {
	private final OrderRepository orderRepo;
	private final PaymentService paymentService;

	@Scheduled(fixedRate = 60000)
	public void cleanupExpiredOrders() {
		try {
			LocalDateTime now = LocalDateTime.now();
			List<Orders> expiredOrders = new ArrayList<>();
			expiredOrders.addAll(orderRepo.findAllByOrderStatusAndExpiredTimeBefore(OrderStatus.PENDING, now));
			expiredOrders.addAll(orderRepo.findAllByOrderStatusAndExpiredTimeBefore(OrderStatus.IN_PROGRESS, now));

			if (expiredOrders.isEmpty()) {
				return;
			}

			for (Orders order : expiredOrders) {
				try {
					paymentService.processFail(order, OrderStatus.EXPIRED);
				} catch (Exception e) {
					log.error("Failed to process expired order {}: {}", order.getOrderId(), e.getMessage(), e);
				}
			}

		} catch (Exception e) {
			log.error("Error during cleanup process: {}", e.getMessage(), e);
		}
	}
}