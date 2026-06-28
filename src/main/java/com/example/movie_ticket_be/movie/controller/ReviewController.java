package com.example.movie_ticket_be.movie.controller;

import org.springframework.data.domain.Page;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.request.ReviewRequest;
import com.example.movie_ticket_be.movie.dto.response.MovieReviewPageResponse;
import com.example.movie_ticket_be.movie.dto.response.ReviewResponse;
import com.example.movie_ticket_be.movie.dto.response.UnreviewedMovieResponse;
import com.example.movie_ticket_be.movie.service.ReviewInteractionService;
import com.example.movie_ticket_be.movie.service.ReviewService;
import com.example.movie_ticket_be.user.enums.InteractionType;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReviewController {

    ReviewService reviewService;
    ReviewInteractionService reviewInteractionService;

    @PostMapping
    public ApiResponse<ReviewResponse> createReview(@RequestBody ReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.createReview(request))
                .build();
    }

    @PutMapping("/{reviewId}")
    public ApiResponse<ReviewResponse> updateReview(
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request) {
        return ApiResponse.<ReviewResponse>builder()
                .result(reviewService.updateReview(reviewId, request))
                .build();
    }

    @GetMapping("/movie/{movieId}")
    public ApiResponse<MovieReviewPageResponse> getReviewsByMovie(
            @PathVariable Long movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer rating) {
        return ApiResponse.<MovieReviewPageResponse>builder()
                .result(reviewService.getReviewsByMovie(movieId, page, size, rating))
                .build();
    }

    @PostMapping("/{reviewId}/interactions/{type}")
    public ApiResponse<Void> toggleInteraction(
            @PathVariable Long reviewId,
            @PathVariable InteractionType type) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        reviewInteractionService.toggleInteraction(reviewId, type, username);
        return ApiResponse.<Void>builder().build();
    }

    @GetMapping("/recent-unreviewed")
    public ApiResponse<List<UnreviewedMovieResponse>> getRecentUnreviewedMovie() {
        return ApiResponse.<List<UnreviewedMovieResponse>>builder()
                .result(reviewService.getRecentUnreviewedMovie())
                .build();
    }
}
