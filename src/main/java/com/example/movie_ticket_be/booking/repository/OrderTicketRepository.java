package com.example.movie_ticket_be.booking.repository;

import com.example.movie_ticket_be.booking.entity.OrderTickets;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderTicketRepository extends JpaRepository<OrderTickets,String> {
    List<OrderTickets> findByOrders_OrderId(Long ordersOrderId);
    Optional<OrderTickets> findBySeatShowTime_SeatShowTimeId(Long seatShowTimeId);
    void deleteByOrders_OrderId(Long ordersOrderId);
}
