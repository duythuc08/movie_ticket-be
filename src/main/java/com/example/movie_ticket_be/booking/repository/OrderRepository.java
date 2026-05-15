package com.example.movie_ticket_be.booking.repository;

import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders,Long> {
    Optional<Orders> findByOrderId(Long orderId);

    @EntityGraph(attributePaths = {
            "users",
            "orderTickets",
            "orderTickets.seatShowTime.seats",
            "orderTickets.seatShowTime.seats.rooms",
            "orderTickets.seatShowTime.showTimes.movies",
            "orderTickets.seatShowTime.showTimes.rooms",
            "orderTickets.seatShowTime.showTimes.rooms.cinemas",
            "orderFoods.foods"
    })
    List<Orders> findByUsers_UserId(String usersUserId);
    List<Orders> findAllByOrderStatusAndExpiredTimeBefore(OrderStatus orderStatus, LocalDateTime expiredTimeBefore);
}
