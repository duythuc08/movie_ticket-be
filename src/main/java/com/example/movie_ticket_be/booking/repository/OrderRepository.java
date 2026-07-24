package com.example.movie_ticket_be.booking.repository;

import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Orders, Long>, JpaSpecificationExecutor<Orders> {

	@EntityGraph(attributePaths = {"users", "orderTickets", "orderTickets.seatShowTime.seats",
			"orderTickets.seatShowTime.showTimes.movies", "orderTickets.seatShowTime.showTimes.rooms.cinemas",
			"orderFoods.foods"})
	Optional<Orders> findByOrderId(Long orderId);

	@EntityGraph(attributePaths = {"users", "orderTickets", "orderTickets.seatShowTime.seats",
			"orderTickets.seatShowTime.showTimes.movies", "orderTickets.seatShowTime.showTimes.rooms.cinemas",
			"orderFoods.foods"})
	List<Orders> findByUsers_UserId(String usersUserId);

	@EntityGraph(attributePaths = {"users", "orderTickets", "orderTickets.seatShowTime.seats",
			"orderTickets.seatShowTime.showTimes.movies", "orderTickets.seatShowTime.showTimes.rooms.cinemas",
			"orderFoods.foods"})
	List<Orders> findByUsers_UserIdAndOrderStatus(String usersUserId, OrderStatus orderStatus);

	boolean existsByOrderIdAndUsers_Username(Long orderId, String username);

	boolean existsByUsers_UserIdAndUsers_Username(String userId, String username);

	List<Orders> findAllByOrderStatusAndExpiredTimeBefore(OrderStatus orderStatus, LocalDateTime expiredTimeBefore);

	/** Load order không kèm EntityGraph — dùng trong releaseBooking để tránh eager-load orderTickets */
	@Query("SELECT o FROM Orders o WHERE o.orderId = :orderId")
	Optional<Orders> findByOrderIdPlain(@Param("orderId") Long orderId);

	/** Pessimistic lock — dùng trong checkout để chặn 2 tab gọi đồng thời */
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT o FROM Orders o WHERE o.orderId = :orderId")
	Optional<Orders> findByOrderIdForUpdate(@Param("orderId") Long orderId);

	@Modifying
	@Transactional
	@Query("UPDATE Orders o SET o.orderStatus = :status, o.updatedAt = :now WHERE o.orderId = :orderId")
	void updateOrderStatus(@Param("orderId") Long orderId,
	                       @Param("status") OrderStatus status,
	                       @Param("now") LocalDateTime now);

	@Query("SELECT SUM(o.finalPrice) FROM Orders o WHERE o.orderStatus = :status AND o.bookingTime >= :from AND o.bookingTime <= :to")
	BigDecimal sumRevenue(@Param("status") OrderStatus status, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);

	@Query("SELECT COUNT(o) FROM Orders o WHERE o.bookingTime >= :from AND o.bookingTime <= :to")
	Long countByPeriod(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

	@Query("SELECT COUNT(o) FROM Orders o WHERE o.orderStatus = :status AND o.bookingTime >= :from AND o.bookingTime <= :to")
	Long countByStatusAndPeriod(@Param("status") OrderStatus status, @Param("from") LocalDateTime from,
			@Param("to") LocalDateTime to);

	@Query("SELECT DAY(o.bookingTime), SUM(o.finalPrice), COUNT(o) FROM Orders o WHERE o.orderStatus = :status AND o.bookingTime >= :from AND o.bookingTime <= :to GROUP BY DAY(o.bookingTime)")
	List<Object[]> sumRevenueAndOrdersPerDay(@Param("status") OrderStatus status, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}
