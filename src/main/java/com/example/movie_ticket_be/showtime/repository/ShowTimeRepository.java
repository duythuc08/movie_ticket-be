package com.example.movie_ticket_be.showtime.repository;

import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.showtime.entity.SeatShowTime;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import com.example.movie_ticket_be.showtime.enums.ShowTimeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ShowTimeRepository extends JpaRepository<ShowTimes, String> {

    @Query("SELECT s FROM ShowTimes s " +
            "LEFT JOIN FETCH s.movies " +
            "LEFT JOIN FETCH s.rooms r " +
            "LEFT JOIN FETCH r.cinemas")
    List<ShowTimes> findAllWithDetails();


    List<ShowTimes> findByMovies_MovieIdAndStartTimeBetween(Long movieId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT s FROM ShowTimes s " +
            "WHERE s.rooms.cinemas.cinemaId = :cinemaId " +
            "AND s.movies.movieId = :movieId " +
            "AND s.startTime > :now")
    List<ShowTimes> findByRooms_Cinemas_CinemaIdAndMovies_MovieIdAndStartTimeAfter(
            @Param("cinemaId") Long cinemaId,
            @Param("movieId") Long movieId,
            @Param("now") LocalDateTime now);


    List<ShowTimes> findByMovies_MovieIdAndStartTimeBetweenAndShowTimeStatusNot(
            Long movieId,
            LocalDateTime start,
            LocalDateTime end,
            ShowTimeStatus status
    );

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END " +
            "FROM ShowTimes s " +
            "WHERE s.rooms.roomId = :roomId " +
            "AND s.showTimeStatus != 'CANCELLED' " +
            "AND (:newItemStart < s.endTime AND :newItemEnd > s.startTime)")
    boolean existsConflictingShowtime(@Param("roomId") Long roomId,
                                      @Param("newItemStart") LocalDateTime newItemStart,
                                      @Param("newItemEnd") LocalDateTime newItemEnd);

    List<ShowTimes> findByMovies_MovieId(Long movieId);

    boolean existsByShowTimeId(Long showTimeId);

    java.util.Optional<ShowTimes> findByShowTimeId(Long showTimeId);

    List<ShowTimes> findByShowTimeStatusAndStartTimeLessThanEqual(ShowTimeStatus status, LocalDateTime time);

    List<ShowTimes> findByShowTimeStatusAndEndTimeLessThanEqual(ShowTimeStatus status, LocalDateTime time);

    @Query("SELECT s FROM ShowTimes s WHERE s.showTimeStatus IN ('SCHEDULED', 'ONGOING') " +
            "AND NOT EXISTS (SELECT ss FROM SeatShowTime ss WHERE ss.showTimes = s AND ss.seatShowTimeStatus = 'AVAILABLE')")
    List<ShowTimes> findFullyBookedCandidates();


    @Query("SELECT DISTINCT s.movies FROM ShowTimes s " +
            "JOIN s.rooms r JOIN r.cinemas c " +
            "WHERE c.cinemaId = :cinemaId " +
            "AND s.showTimeStatus != 'CANCELLED' " +
            "AND (s.movies.movieStatus IN :activeStatuses OR s.startTime >= :startOfToday)")
    List<Movies> findDistinctMoviesByCinemaId(
            @Param("cinemaId") Long cinemaId,
            @Param("startOfToday") LocalDateTime startOfToday,
            @Param("activeStatuses") List<MovieStatus> activeStatuses);

    // Quick Booking Bar: lấy các suất chiếu từ đầu ngày hôm nay của một phim tại rạp
    @Query("SELECT s FROM ShowTimes s " +
            "JOIN s.rooms r JOIN r.cinemas c " +
            "WHERE c.cinemaId = :cinemaId " +
            "AND s.movies.movieId = :movieId " +
            "AND s.startTime >= :startOfToday " +
            "AND s.showTimeStatus != 'CANCELLED' " +
            "ORDER BY s.startTime ASC")
    List<ShowTimes> findByCinemaIdAndMovieIdAfterNow(
            @Param("cinemaId") Long cinemaId,
            @Param("movieId") Long movieId,
            @Param("startOfToday") LocalDateTime startOfToday);

    @Query("SELECT s FROM ShowTimes s " +
            "JOIN s.rooms r JOIN r.cinemas c " +
            "WHERE c.cinemaId = :cinemaId " +
            "AND s.movies.movieId = :movieId " +
            "AND s.startTime >= :startOfDay " +
            "AND s.startTime <= :endOfDay " +
            "AND s.showTimeStatus != 'CANCELLED' " +
            "ORDER BY s.startTime ASC")
    List<ShowTimes> findByCinemaIdAndMovieIdOnDate(
            @Param("cinemaId") Long cinemaId,
            @Param("movieId") Long movieId,
            @Param("startOfDay") LocalDateTime startOfDay,
            @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT DISTINCT s.movies FROM ShowTimes s " +
            "WHERE s.showTimeStatus != 'CANCELLED' " +
            "AND s.startTime >= :now " +
            "AND s.movies.movieStatus IN :activeStatuses")
    List<Movies> findNowShowingMoviesWithUpcomingSlots(
            @Param("now") LocalDateTime now,
            @Param("activeStatuses") List<MovieStatus> activeStatuses);

    @Query("SELECT DISTINCT r.cinemas FROM ShowTimes s " +
            "JOIN s.rooms r JOIN r.cinemas c " +
            "WHERE s.movies.movieId = :movieId " +
            "AND s.showTimeStatus != 'CANCELLED' " +
            "AND s.startTime >= :now")
    List<Cinemas> findDistinctCinemasByMovieId(
            @Param("movieId") Long movieId,
            @Param("now") LocalDateTime now);
}