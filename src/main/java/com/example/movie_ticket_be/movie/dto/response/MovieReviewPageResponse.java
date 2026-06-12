package com.example.movie_ticket_be.movie.dto.response;

import java.util.List;
import java.util.Map;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MovieReviewPageResponse {
    double averageRating;
    long totalReviews;
    Map<Integer, Long> ratingDistribution;

    List<ReviewResponse> content;
    int currentPage;
    int totalPages;
    long totalElements;
    boolean last;
}
