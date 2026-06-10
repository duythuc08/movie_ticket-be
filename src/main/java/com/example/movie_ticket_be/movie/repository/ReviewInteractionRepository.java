package com.example.movie_ticket_be.movie.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.movie_ticket_be.movie.entity.ReviewInteractions;
import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.user.entity.Users;

public interface ReviewInteractionRepository extends JpaRepository<ReviewInteractions, Long> {

    Optional<ReviewInteractions> findByUsersAndReviews(Users users, Reviews reviews);

    List<ReviewInteractions> findByUsersAndReviewsIn(Users users, List<Reviews> reviews);
}
