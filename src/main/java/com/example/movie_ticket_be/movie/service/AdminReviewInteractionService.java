package com.example.movie_ticket_be.movie.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.movie.dto.response.AdminReviewInteractionResponse;
import com.example.movie_ticket_be.movie.entity.ReviewInteractions;
import com.example.movie_ticket_be.movie.mapper.ReviewInteractionMapper;
import com.example.movie_ticket_be.movie.repository.ReviewInteractionRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminReviewInteractionService {
    ReviewInteractionRepository reviewInteractionRepository;
    ReviewInteractionMapper reviewInteractionMapper;

    public List<AdminReviewInteractionResponse> getAllReviewInteractions(Long reviewId) {
        List<ReviewInteractions> reviewInteractions = reviewInteractionRepository.findByReviews_ReviewIdAndIsActiveTrue(reviewId);
        return reviewInteractionMapper.toAdminReviewInteractionResponse(reviewInteractions);
    }
}