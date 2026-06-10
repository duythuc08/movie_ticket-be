package com.example.movie_ticket_be.booking.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.movie_ticket_be.booking.entity.OrderTickets;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.user.entity.Users;

public interface OrderTicketRepository extends JpaRepository<OrderTickets, Long> {

	List<OrderTickets> findByOrders_OrderId(Long ordersOrderId);

	Optional<OrderTickets> findBySeatShowTime_SeatShowTimeId(Long seatShowTimeId);

	void deleteByOrders_OrderId(Long ordersOrderId);

	@Query("SELECT COUNT(ot) FROM OrderTickets ot " +
			"WHERE ot.orders.users = :user " +
			"AND ot.seatShowTime.showTimes.movies = :movie " +
			"AND ot.seatShowTime.showTimes.endTime < :now " +
			"AND ot.orders.orderStatus IN :statuses")
	Long countCompletedBookingByUserAndMovie(
			@Param("user") Users user,
			@Param("movie") Movies movie,
			@Param("now") LocalDateTime now,
			@Param("statuses") Collection<OrderStatus> statuses);
}
