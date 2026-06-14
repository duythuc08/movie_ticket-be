package com.example.movie_ticket_be.movie.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.response.AdminReviewInteractionResponse;
import com.example.movie_ticket_be.movie.dto.response.AdminReviewResponse;
import com.example.movie_ticket_be.movie.enums.ReviewStatus;
import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.movie.service.AdminReviewInteractionService;
import com.example.movie_ticket_be.movie.service.AdminReviewService;

import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/admin/reviews")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminReviewController {

    AdminReviewService adminReviewService;
    AdminReviewInteractionService adminReviewInteractionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<AdminReviewResponse>> getAdminReviews(
            @Parameter(name = "filter", required = false) @Filter Specification<Reviews> spec,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<AdminReviewResponse>>builder()
                .result(adminReviewService.getAdminReviews(spec, pageable))
                .build();
    }

    @PatchMapping("/{reviewId}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminReviewResponse> approveReview(@PathVariable Long reviewId) {
        return ApiResponse.<AdminReviewResponse>builder()
                .result(adminReviewService.approveReview(reviewId))
                .build();
    }

    @PatchMapping("/{reviewId}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminReviewResponse> rejectReview(@PathVariable Long reviewId) {
        return ApiResponse.<AdminReviewResponse>builder()
                .result(adminReviewService.rejectReview(reviewId))
                .build();
    }

    @PatchMapping("/{reviewId}/hide")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<AdminReviewResponse> hideReview(@PathVariable Long reviewId) {
        return ApiResponse.<AdminReviewResponse>builder()
                .result(adminReviewService.hideReview(reviewId))
                .build();
    }

    @GetMapping("/{reviewId}/interactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<AdminReviewInteractionResponse>> getReviewInteractions(@PathVariable Long reviewId) {
        return ApiResponse.<List<AdminReviewInteractionResponse>>builder()
                .result(adminReviewInteractionService.getAllReviewInteractions(reviewId))
                .build();
    }
}
