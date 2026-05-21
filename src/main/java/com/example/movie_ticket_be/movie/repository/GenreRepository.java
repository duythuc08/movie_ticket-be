package com.example.movie_ticket_be.movie.repository;


import com.example.movie_ticket_be.movie.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre,Long>, JpaSpecificationExecutor<Genre> {
    boolean existsByName(String name);
    Optional<Genre> findByName(String name);
    
}
