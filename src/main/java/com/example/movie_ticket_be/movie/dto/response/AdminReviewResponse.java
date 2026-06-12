package com.example.movie_ticket_be.movie.dto.response;

import java.time.LocalDateTime;

import com.example.movie_ticket_be.movie.enums.ReviewStatus;

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
public class AdminReviewResponse {
    Long reviewId;
    Long movieId;
    String movieTitle;
    String userId;
    String username;
    String fullName;
    Integer rating;
    String comment;
    Integer likeCount;
    Integer dislikeCount;
    ReviewStatus reviewStatus;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
