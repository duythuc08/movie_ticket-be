package com.example.movie_ticket_be.cinema.repository;

import com.example.movie_ticket_be.cinema.entity.Seats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seats, Long> {
    Optional<Seats> findBySeatId(Long seatId);
    boolean existsBySeatId(Long seatId);
    List<Seats> findByRooms_RoomId(Long roomId);
    boolean existsByRooms_RoomId(Long roomId);
    boolean existsBySeatRowAndSeatNumberAndRooms_RoomId(String seatRow, Integer seatNumber, Long roomId);

    Optional<Seats> findBySeatRowAndSeatNumberAndRooms_RoomId(String seatRow, Integer seatNumber, Long roomId);

    @Modifying
    @Query("DELETE FROM Seats s WHERE s.rooms.roomId = :roomId")
    void deleteAllByRoomId(@Param("roomId") Long roomId);
}
