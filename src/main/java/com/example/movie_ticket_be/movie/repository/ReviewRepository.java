package com.example.movie_ticket_be.movie.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.movie.enums.ReviewStatus;
import com.example.movie_ticket_be.user.entity.Users;

public interface ReviewRepository extends JpaRepository<Reviews, Long>, JpaSpecificationExecutor<Reviews> {

    boolean existsByUsersAndMovies(Users users, Movies movies);

    Page<Reviews> findByMoviesAndReviewStatus(Movies movies, ReviewStatus status, Pageable pageable);

    Page<Reviews> findByReviewStatus(ReviewStatus status, Pageable pageable);

    Page<Reviews> findByMovies(Movies movies, Pageable pageable);

    Optional<Reviews> findByReviewId(Long reviewId);
}
