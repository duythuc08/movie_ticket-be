
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

	@Query("SELECT m FROM Movies m WHERE m.movieStatus = 'NOW_SHOWING' " 
        + "AND m.entityStatus = 'ACTIVE' "
        + "AND NOT EXISTS (SELECT s FROM ShowTimes s WHERE s.movies = m "
        + "AND s.showTimeStatus IN ('SCHEDULED', 'FULLY_BOOKED', 'ONGOING', 'COMPLETED') "
        + "AND s.startTime > :threeDaysAgo)")
	List<Movies> findNowShowingWithNoFutureShowtimes(@Param("threeDaysAgo") LocalDateTime threeDaysAgo);

	@Query("SELECT DISTINCT ot.seatShowTime.showTimes.movies FROM OrderTickets ot " +
			"WHERE ot.orders.users.userId = :userId " +
			"AND ot.seatShowTime.showTimes.showTimeStatus = 'COMPLETED' " +
			"AND ot.seatShowTime.showTimes.movies.movieStatus = 'NOW_SHOWING' " +
			"AND ot.seatShowTime.showTimes.startTime <= :now " +
			"AND ot.seatShowTime.showTimes.movies.movieId NOT IN (" +
			"    SELECT r.movies.movieId FROM Reviews r WHERE r.users.userId = :userId" +
			")")
	List<Movies> findUnreviewedActiveMovies(
			@Param("userId") String userId,
			@Param("now") LocalDateTime now,
			Pageable pageable
	);
}
