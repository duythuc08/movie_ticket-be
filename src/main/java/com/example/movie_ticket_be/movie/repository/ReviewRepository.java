package com.example.movie_ticket_be.movie.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.movie.enums.ReviewStatus;
import com.example.movie_ticket_be.user.entity.Users;

public interface ReviewRepository extends JpaRepository<Reviews, Long>, JpaSpecificationExecutor<Reviews> {

    boolean existsByUsersAndMovies(Users users, Movies movies);

    Page<Reviews> findByMoviesAndReviewStatus(Movies movies, ReviewStatus status, Pageable pageable);

    Page<Reviews> findByMoviesAndReviewStatusAndRating(Movies movies, ReviewStatus status, Integer rating, Pageable pageable);

    Page<Reviews> findByReviewStatus(ReviewStatus status, Pageable pageable);

    Page<Reviews> findByMovies(Movies movies, Pageable pageable);

    Optional<Reviews> findByReviewId(Long reviewId);

    @Query("SELECT r.rating, COUNT(r) FROM Reviews r WHERE r.movies = :movie AND r.reviewStatus = :status GROUP BY r.rating")
    List<Object[]> countGroupByRating(@Param("movie") Movies movie, @Param("status") ReviewStatus status);

    @Query("""
        SELECT r from Reviews r
        WHERE r.users.userId = :userId AND r.movies.movieId = :movieId 
            AND r.reviewStatus = 'APPROVED'
    """)
    Optional<Reviews> findApprovalReview(@Param("userId") String userId, @Param("movieId") Long movieId);

    @Query("""
        SELECT r from Reviews r
        JOIN FETCH r.movies
        WHERE r.users.userId = :userId AND r.reviewStatus = 'APPROVED'
    """)
    List<Reviews> findApprovalReviewsByUserId(@Param("userId") String userId);

    @Query("""
        SELECT COUNT(r) from Reviews r
        WHERE r.users.userId = :userId AND r.reviewStatus = 'APPROVED'
    """)
    Long countApprovalReviewsByUser(@Param("userId") String userId);
}
