package com.example.movie_ticket_be.cinema.repository;

import com.example.movie_ticket_be.cinema.entity.Seats;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeatRepository extends JpaRepository<Seats, Long> {
    Optional<Seats> findBySeatId(Long seatId);
    boolean existsBySeatId(Long seatId);
    List<Seats> findByRooms_RoomId(Long roomId);
}
