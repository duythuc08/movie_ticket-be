
package com.example.movie_ticket_be.movie.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.enums.MovieStatus;

public interface MovieRepository extends JpaRepository<Movies, Long>, JpaSpecificationExecutor<Movies> {
	boolean existsByTitle(String title);

	boolean existsByMovieId(Long movieId);

	List<Movies> findByEntityStatusAndMovieStatus(EntityStatus entityStatus, MovieStatus movieStatus);

	Page<Movies> findByEntityStatusAndMovieStatus(EntityStatus entityStatus, MovieStatus movieStatus,
			Pageable pageable);

	Optional<Movies> findByMovieId(Long id);

	List<Movies> findByTitleContainingIgnoreCase(String keyword);

	List<Movies> findAllByMovieStatusAndReleaseDateBefore(MovieStatus status, LocalDateTime dateTime);

	@Query("SELECT m FROM Movies m WHERE m.movieStatus = 'NOW_SHOWING' " + "AND m.entityStatus = 'ACTIVE' "
			+ "AND NOT EXISTS (SELECT s FROM ShowTimes s WHERE s.movies = m "
			+ "AND s.showTimeStatus IN ('SCHEDULED', 'FULLY_BOOKED') " + "AND s.startTime > :now)")
	List<Movies> findNowShowingWithNoFutureShowtimes(@Param("now") LocalDateTime now);
}
