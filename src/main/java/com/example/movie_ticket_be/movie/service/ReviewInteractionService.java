package com.example.movie_ticket_be.movie.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.entity.ReviewInteractions;
import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.movie.repository.ReviewInteractionRepository;
import com.example.movie_ticket_be.movie.repository.ReviewRepository;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.enums.InteractionType;
import com.example.movie_ticket_be.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewInteractionService {

    ReviewRepository reviewRepository;
    ReviewInteractionRepository interactionRepository;
    UserRepository userRepository;

    @Transactional
    public void toggleInteraction(Long reviewId, InteractionType newType, String username) {
        Users user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));

        Optional<ReviewInteractions> existing = interactionRepository.findByUsersAndReviews(user, review);

        if (existing.isPresent()) {
            ReviewInteractions interaction = existing.get();
            if (interaction.isActive()) {
                if (interaction.getInteractionType() == newType) {
                    interaction.setActive(false);
                    interactionRepository.save(interaction);
                    decreaseCount(review, newType);
                } else {
                    decreaseCount(review, interaction.getInteractionType());
                    interaction.setInteractionType(newType);
                    interactionRepository.save(interaction);
                    increaseCount(review, newType);
                }
            } else {
                interaction.setInteractionType(newType);
                interaction.setActive(true);
                interactionRepository.save(interaction);
                increaseCount(review, newType);
            }
        } else {
            ReviewInteractions newInteraction = new ReviewInteractions();
            newInteraction.setUsers(user);
            newInteraction.setReviews(review);
            newInteraction.setInteractionType(newType);
            newInteraction.setActive(true);
            interactionRepository.save(newInteraction);
            increaseCount(review, newType);
        }

        reviewRepository.save(review);
    }

    private void increaseCount(Reviews review, InteractionType type) {
        if (type == InteractionType.LIKE) {
            review.setLikeCount(review.getLikeCount() + 1);
        } else {
            review.setDislikeCount(review.getDislikeCount() + 1);
        }
    }

    private void decreaseCount(Reviews review, InteractionType type) {
        if (type == InteractionType.LIKE) {
            review.setLikeCount(Math.max(0, review.getLikeCount() - 1));
        } else {
            review.setDislikeCount(Math.max(0, review.getDislikeCount() - 1));
        }
    }
}
