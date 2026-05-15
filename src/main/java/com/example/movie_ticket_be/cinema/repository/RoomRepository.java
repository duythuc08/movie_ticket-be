package com.example.movie_ticket_be.cinema.repository;

import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.enums.RoomStatus;
import com.example.movie_ticket_be.cinema.enums.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Rooms, Long> {

    List<Rooms> findByCinemas_CinemaId(Long cinemaId);

    List<Rooms> findByCinemas_CinemaIdAndRoomStatus(Long cinemaId, RoomStatus roomStatus);

    List<Rooms> findByCinemas_CinemaIdAndRoomType(Long cinemaId, RoomType roomType);

    boolean existsByNameAndCinemas_CinemaId(String name, Long cinemaId);

    List<Rooms> findByCinemas_CinemaIdAndNameContainingIgnoreCase(Long cinemaId, String keyword);

    Optional<Rooms> findByRoomId(Long roomId);

}