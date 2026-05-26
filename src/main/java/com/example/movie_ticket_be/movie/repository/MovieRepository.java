
package com.example.movie_ticket_be.movie.repository;

import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;

public interface MovieRepository extends JpaRepository<Movies,Long>, JpaSpecificationExecutor<Movies> {
    boolean existsByTitle(String title);

    boolean existsByMovieId(Long movieId);

    List<Movies> findByEntityStatusAndMovieStatus(EntityStatus entityStatus, MovieStatus movieStatus);

    Page<Movies> findByEntityStatusAndMovieStatus(EntityStatus entityStatus, MovieStatus movieStatus, Pageable pageable);

    Optional<Movies> findByMovieId(Long id);

    List<Movies> findByTitleContainingIgnoreCase(String keyword);
}
