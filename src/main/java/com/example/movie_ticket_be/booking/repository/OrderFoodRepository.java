package com.example.movie_ticket_be.booking.repository;

import com.example.movie_ticket_be.booking.entity.OrderFoods;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderFoodRepository extends JpaRepository<OrderFoods,Long> {
    void deleteByOrders_OrderId(Long ordersOrderId);
}
