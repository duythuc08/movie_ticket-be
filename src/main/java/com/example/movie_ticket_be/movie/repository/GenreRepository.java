package com.example.movie_ticket_be.movie.repository;


import com.example.movie_ticket_be.movie.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre,String> {
    boolean existsByName(String name);
    Optional<Genre> findByName(String name);
}
