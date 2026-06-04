package com.example.movie_ticket_be.cinema.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.enums.RoomStatus;
import com.example.movie_ticket_be.cinema.enums.RoomType;

@Repository
public interface RoomRepository extends JpaRepository<Rooms, Long>, JpaSpecificationExecutor<Rooms> {

	List<Rooms> findByCinemas_CinemaId(Long cinemaId);

	List<Rooms> findByCinemas_CinemaIdAndRoomStatus(Long cinemaId, RoomStatus roomStatus);

	List<Rooms> findByCinemas_CinemaIdAndRoomType(Long cinemaId, RoomType roomType);

	boolean existsByNameAndCinemas_CinemaId(String name, Long cinemaId);

	List<Rooms> findByCinemas_CinemaIdAndNameContainingIgnoreCase(Long cinemaId, String keyword);

	Optional<Rooms> findByRoomId(Long roomId);

	boolean existsByNameAndCinemas_CinemaIdAndRoomIdNot(String name, Long cinemaId, Long roomId);

}