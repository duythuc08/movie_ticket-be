package com.example.movie_ticket_be.movie.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.response.AdminReviewResponse;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.movie.enums.ReviewStatus;
import com.example.movie_ticket_be.movie.mapper.ReviewMapper;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.movie.repository.ReviewRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminReviewService {

    ReviewRepository reviewRepository;
    MovieRepository movieRepository;
    ReviewMapper reviewMapper;

    public Page<AdminReviewResponse> getAdminReviews(org.springframework.data.jpa.domain.Specification<Reviews> spec, org.springframework.data.domain.Pageable pageable) {
        return reviewRepository.findAll(spec, pageable).map(reviewMapper::toAdminReviewResponse);
    }

    @Transactional
    public AdminReviewResponse approveReview(Long reviewId) {
        return changeStatus(reviewId, ReviewStatus.APPROVED);
    }

    @Transactional
    public AdminReviewResponse rejectReview(Long reviewId) {
        return changeStatus(reviewId, ReviewStatus.REJECTED);
    }

    @Transactional
    public AdminReviewResponse hideReview(Long reviewId) {
        return changeStatus(reviewId, ReviewStatus.HIDDEN);
    }

    private AdminReviewResponse changeStatus(Long reviewId, ReviewStatus status) {
        Reviews review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        review.setReviewStatus(status);
        return reviewMapper.toAdminReviewResponse(reviewRepository.save(review));
    }
}
