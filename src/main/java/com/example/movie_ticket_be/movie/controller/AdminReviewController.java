package com.example.movie_ticket_be.movie.controller;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.response.AdminReviewResponse;
import com.example.movie_ticket_be.movie.enums.ReviewStatus;
import com.example.movie_ticket_be.movie.service.AdminReviewService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminReviewController {

    AdminReviewService adminReviewService;

    @GetMapping
    public ApiResponse<Page<AdminReviewResponse>> getAdminReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) Long movieId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.<Page<AdminReviewResponse>>builder()
                .result(adminReviewService.getAdminReviews(status, movieId, page, size))
                .build();
    }

    @PatchMapping("/{reviewId}/approve")
    public ApiResponse<AdminReviewResponse> approveReview(@PathVariable Long reviewId) {
        return ApiResponse.<AdminReviewResponse>builder()
                .result(adminReviewService.approveReview(reviewId))
                .build();
    }

    @PatchMapping("/{reviewId}/reject")
    public ApiResponse<AdminReviewResponse> rejectReview(@PathVariable Long reviewId) {
        return ApiResponse.<AdminReviewResponse>builder()
                .result(adminReviewService.rejectReview(reviewId))
                .build();
    }

    @PatchMapping("/{reviewId}/hide")
    public ApiResponse<AdminReviewResponse> hideReview(@PathVariable Long reviewId) {
        return ApiResponse.<AdminReviewResponse>builder()
                .result(adminReviewService.hideReview(reviewId))
                .build();
    }
}
