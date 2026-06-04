package com.example.movie_ticket_be.booking.repository;

import com.example.movie_ticket_be.booking.entity.OrderFoods;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderFoodRepository extends JpaRepository<OrderFoods, Long> {
	void deleteByOrders_OrderId(Long ordersOrderId);
	List<OrderFoods> findByOrders_OrderId(Long orderId);
}
