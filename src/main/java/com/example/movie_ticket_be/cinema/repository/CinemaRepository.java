package com.example.movie_ticket_be.cinema.repository;

import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CinemaRepository extends JpaRepository<Cinemas,String> {
    boolean existsByName(String name);

    List<Cinemas> findByCinemaStatus(CinemaStatus status);

    Optional<Cinemas> findByCinemaId(Long cinemaId);
    List<Cinemas> findByNameContainingIgnoreCase(String name);
}
