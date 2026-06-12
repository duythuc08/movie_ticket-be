package com.example.movie_ticket_be.movie.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.boot.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.movie_ticket_be.movie.entity.ReviewInteractions;
import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.user.entity.Users;
@Repository
public interface ReviewInteractionRepository extends JpaRepository<ReviewInteractions, Long> {

    Optional<ReviewInteractions> findByUsersAndReviews(Users users, Reviews reviews);

    List<ReviewInteractions> findByUsersAndReviewsInAndIsActiveTrue(Users users, List<Reviews> reviews);

    List<ReviewInteractions> findByReviews_ReviewIdAndIsActiveTrue(Long reviewId);
}
