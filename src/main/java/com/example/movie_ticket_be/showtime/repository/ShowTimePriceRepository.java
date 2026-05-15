package com.example.movie_ticket_be.showtime.repository;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.showtime.entity.ShowTimePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShowTimePriceRepository extends JpaRepository<ShowTimePrice, String> {

    boolean existsByShowtimes_ShowTimeIdAndSeatType(Long showtimesShowTimeId, SeatType seatType);

    List<ShowTimePrice> findByShowtimes_ShowTimeId(Long ShowTimeId);

    Optional<ShowTimePrice> findByShowtimes_ShowTimeIdAndSeatType(Long showTimeId, SeatType seatType);
}