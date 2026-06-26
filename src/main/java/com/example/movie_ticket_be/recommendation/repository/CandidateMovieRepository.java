package com.example.movie_ticket_be.recommendation.repository;


import com.example.movie_ticket_be.movie.entity.Movies;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 candidate_movies(u) theo ĐÚNG Mục 2.6 DAC_TA:
 *   excluded_movies(u) = { i : has_explicit(u,i) = TRUE }
 *                      ∪ { i : tồn tại BOOK_TICKET(u,i) với order.status = PAID }
 *   candidate_movies(u) = { phim NOW_SHOWING/COMING_SOON } − excluded_movies(u)
 *
 * Phim mà user chỉ có implicit score từ WATCH_TRAILER/VIEW_DETAILS (xem
 * nhưng chưa rating/đặt vé) VẪN nằm trong candidate
 */
public interface CandidateMovieRepository extends JpaRepository<Movies, Long> {

    @Query(value = """
            SELECT m.movie_id FROM movie m
            WHERE m.movie_status IN ('NOW_SHOWING', 'COMING_SOON')
              AND m.movie_id NOT IN (
                  SELECT r.movie_id FROM review r
                  WHERE r.user_id = :userId AND r.review_status = 'APPROVED'
              )
              AND m.movie_id NOT IN (
                  SELECT st.movie_id
                  FROM orders o
                  JOIN order_ticket ot ON ot.order_id = o.order_id
                  JOIN seat_show_time sst ON sst.seat_show_time_id = ot.seat_show_time_id
                  JOIN show_time st ON st.show_time_id = sst.show_time_id
                  WHERE o.user_id = :userId AND o.order_status = 'PAID'
              )
            """, nativeQuery = true)
    List<Long> findCandidateMovieIds(@Param("userId") String userId);
}
