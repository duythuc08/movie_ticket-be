package com.example.movie_ticket_be.showtime.repository;

import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import com.example.movie_ticket_be.showtime.enums.ShowTimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ShowTimeRepository extends JpaRepository<ShowTimes, Long>, JpaSpecificationExecutor<ShowTimes> {

	@Query("SELECT s FROM ShowTimes s " + "LEFT JOIN FETCH s.movies " + "LEFT JOIN FETCH s.rooms r "
			+ "LEFT JOIN FETCH r.cinemas")
	List<ShowTimes> findAllWithDetails();

	@Query("SELECT s FROM ShowTimes s " + "JOIN s.rooms r JOIN r.cinemas c " + "WHERE s.movies.movieId = :movieId "
			+ "AND s.startTime BETWEEN :startTime AND :endTime " + "AND s.showTimeStatus = 'SCHEDULED' "
			+ "AND s.movies.entityStatus = 'ACTIVE' "
			+ "AND c.entityStatus = 'ACTIVE' AND c.cinemaStatus = 'OPERATIONAL' "
			+ "AND r.entityStatus = 'ACTIVE' AND r.roomStatus = 'OPERATIONAL'")
	List<ShowTimes> findByMovies_MovieIdAndStartTimeBetween(@Param("movieId") Long movieId,
			@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

	@Query("SELECT s FROM ShowTimes s " + "JOIN s.rooms r JOIN r.cinemas c " + "WHERE c.cinemaId = :cinemaId "
			+ "AND s.movies.movieId = :movieId " + "AND s.startTime > :now "
			+ "AND s.showTimeStatus IN ('SCHEDULED', 'FULLY_BOOKED') " + "AND s.movies.entityStatus = 'ACTIVE' "
			+ "AND c.entityStatus = 'ACTIVE' AND c.cinemaStatus = 'OPERATIONAL' "
			+ "AND r.entityStatus = 'ACTIVE' AND r.roomStatus = 'OPERATIONAL'")
	List<ShowTimes> findByRooms_Cinemas_CinemaIdAndMovies_MovieIdAndStartTimeAfter(@Param("cinemaId") Long cinemaId,
			@Param("movieId") Long movieId, @Param("now") LocalDateTime now);

	@Query("SELECT s FROM ShowTimes s " + "JOIN s.rooms r JOIN r.cinemas c " + "WHERE s.movies.movieId = :movieId "
			+ "AND s.startTime BETWEEN :start AND :end " + "AND s.showTimeStatus <> :status "
			+ "AND s.movies.entityStatus = 'ACTIVE' "
			+ "AND c.entityStatus = 'ACTIVE' AND c.cinemaStatus = 'OPERATIONAL' "
			+ "AND r.entityStatus = 'ACTIVE' AND r.roomStatus = 'OPERATIONAL'")
	List<ShowTimes> findByMovies_MovieIdAndStartTimeBetweenAndShowTimeStatusNot(@Param("movieId") Long movieId,
			@Param("start") LocalDateTime start, @Param("end") LocalDateTime end,
			@Param("status") ShowTimeStatus status);

	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " + "FROM ShowTimes s "
			+ "WHERE s.rooms.roomId = :roomId " + "AND s.showTimeStatus NOT IN ('CANCELLED', 'COMPLETED') "
			+ "AND (:newItemStart < s.endTime AND :newItemEnd > s.startTime)")
	boolean existsConflictingShowtime(@Param("roomId") Long roomId, @Param("newItemStart") LocalDateTime newItemStart,
			@Param("newItemEnd") LocalDateTime newItemEnd);

	@Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " + "FROM ShowTimes s "
			+ "WHERE s.rooms.roomId = :roomId " + "AND s.showTimeId <> :excludeId "
			+ "AND s.showTimeStatus NOT IN ('CANCELLED', 'COMPLETED') "
			+ "AND (:newItemStart < s.endTime AND :newItemEnd > s.startTime)")
	boolean existsConflictingShowtimeExcluding(@Param("roomId") Long roomId,
			@Param("newItemStart") LocalDateTime newItemStart, @Param("newItemEnd") LocalDateTime newItemEnd,
			@Param("excludeId") Long excludeId);

	@Query("SELECT s FROM ShowTimes s " + "JOIN s.rooms r JOIN r.cinemas c " + "WHERE s.movies.movieId = :movieId "
			+ "AND s.showTimeStatus IN ('SCHEDULED', 'FULLY_BOOKED') " + "AND s.movies.entityStatus = 'ACTIVE' "
			+ "AND c.entityStatus = 'ACTIVE' AND c.cinemaStatus = 'OPERATIONAL' "
			+ "AND r.entityStatus = 'ACTIVE' AND r.roomStatus = 'OPERATIONAL'")
	List<ShowTimes> findByMovies_MovieId(@Param("movieId") Long movieId);

	boolean existsByShowTimeId(Long showTimeId);

	java.util.Optional<ShowTimes> findByShowTimeId(Long showTimeId);

	List<ShowTimes> findByShowTimeStatusAndStartTimeLessThanEqual(ShowTimeStatus status, LocalDateTime time);

	List<ShowTimes> findByShowTimeStatusAndEndTimeLessThanEqual(ShowTimeStatus status, LocalDateTime time);

	@Query("SELECT s FROM ShowTimes s WHERE s.showTimeStatus IN ('SCHEDULED', 'ONGOING') "
			+ "AND EXISTS (SELECT ss FROM SeatShowTime ss WHERE ss.showTimes = s) "
			+ "AND NOT EXISTS (SELECT ss FROM SeatShowTime ss WHERE ss.showTimes = s AND ss.seatShowTimeStatus <> 'SOLD')")
	List<ShowTimes> findFullyBookedCandidates();

	@Query("SELECT DISTINCT s.movies FROM ShowTimes s " + "JOIN s.rooms r JOIN r.cinemas c "
			+ "WHERE c.cinemaId = :cinemaId " + "AND s.showTimeStatus IN ('SCHEDULED', 'FULLY_BOOKED') "
			+ "AND s.movies.entityStatus = 'ACTIVE' "
			+ "AND c.entityStatus = 'ACTIVE' AND c.cinemaStatus = 'OPERATIONAL' "
			+ "AND r.entityStatus = 'ACTIVE' AND r.roomStatus = 'OPERATIONAL' "
			+ "AND (s.movies.movieStatus IN :activeStatuses OR s.startTime >= :startOfToday)")
	List<Movies> findDistinctMoviesByCinemaId(@Param("cinemaId") Long cinemaId,
			@Param("startOfToday") LocalDateTime startOfToday,
			@Param("activeStatuses") List<MovieStatus> activeStatuses);

	// Quick Booking Bar: lấy các suất chiếu từ đầu ngày hôm nay của một phim tại
	// rạp
	@Query("SELECT s FROM ShowTimes s " + "JOIN s.rooms r JOIN r.cinemas c " + "WHERE c.cinemaId = :cinemaId "
			+ "AND s.movies.movieId = :movieId " + "AND s.startTime >= :startOfToday "
			+ "AND s.showTimeStatus IN ('SCHEDULED', 'FULLY_BOOKED') " + "AND s.movies.entityStatus = 'ACTIVE' "
			+ "AND c.entityStatus = 'ACTIVE' AND c.cinemaStatus = 'OPERATIONAL' "
			+ "AND r.entityStatus = 'ACTIVE' AND r.roomStatus = 'OPERATIONAL' " + "ORDER BY s.startTime ASC")
	List<ShowTimes> findByCinemaIdAndMovieIdAfterNow(@Param("cinemaId") Long cinemaId, @Param("movieId") Long movieId,
			@Param("startOfToday") LocalDateTime startOfToday);

	@Query("SELECT s FROM ShowTimes s " + "JOIN s.rooms r JOIN r.cinemas c " + "WHERE c.cinemaId = :cinemaId "
			+ "AND s.movies.movieId = :movieId " + "AND s.startTime >= :startOfDay " + "AND s.startTime <= :endOfDay "
			+ "AND s.showTimeStatus IN ('SCHEDULED', 'FULLY_BOOKED') " + "AND s.movies.entityStatus = 'ACTIVE' "
			+ "AND c.entityStatus = 'ACTIVE' AND c.cinemaStatus = 'OPERATIONAL' "
			+ "AND r.entityStatus = 'ACTIVE' AND r.roomStatus = 'OPERATIONAL' " + "ORDER BY s.startTime ASC")
	List<ShowTimes> findByCinemaIdAndMovieIdOnDate(@Param("cinemaId") Long cinemaId, @Param("movieId") Long movieId,
			@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

	@Query("SELECT DISTINCT s.movies FROM ShowTimes s " + "WHERE s.showTimeStatus IN ('SCHEDULED', 'FULLY_BOOKED') "
			+ "AND s.movies.entityStatus = 'ACTIVE' " + "AND s.startTime >= :now "
			+ "AND s.movies.movieStatus IN :activeStatuses")
	List<Movies> findNowShowingMoviesWithUpcomingSlots(@Param("now") LocalDateTime now,
			@Param("activeStatuses") List<MovieStatus> activeStatuses);

	boolean existsByRooms_RoomIdAndShowTimeStatusIn(Long roomId, List<ShowTimeStatus> statuses);

	@Query("SELECT DISTINCT r.cinemas FROM ShowTimes s " + "JOIN s.rooms r JOIN r.cinemas c "
			+ "WHERE s.movies.movieId = :movieId " + "AND s.showTimeStatus IN ('SCHEDULED', 'FULLY_BOOKED') "
			+ "AND s.movies.entityStatus = 'ACTIVE' "
			+ "AND c.entityStatus = 'ACTIVE' AND c.cinemaStatus = 'OPERATIONAL' "
			+ "AND r.entityStatus = 'ACTIVE' AND r.roomStatus = 'OPERATIONAL' " + "AND s.startTime >= :now")
	List<Cinemas> findDistinctCinemasByMovieId(@Param("movieId") Long movieId, @Param("now") LocalDateTime now);

	boolean existsByRooms_Cinemas_CinemaId(Long cinemaId);
	List<ShowTimes> findByRooms_Cinemas_CinemaIdAndStartTimeBetween(Long cinemaId, LocalDateTime start,
			LocalDateTime end);

	List<ShowTimes> findByMovies_MovieIdAndShowTimeStatusIn(Long movieId, List<ShowTimeStatus> statuses);
}