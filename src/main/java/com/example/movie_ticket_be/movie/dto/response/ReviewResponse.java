package com.example.movie_ticket_be.movie.dto.response;

import java.time.LocalDateTime;

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
public class ReviewResponse {
    Long reviewId;
    Long movieId;
    String userId;
    String username;
    String fullName;
    Integer rating;
    String comment;
    Integer likeCount;
    Integer dislikeCount;
    LocalDateTime createdAt;
    boolean isLikedByMe;
    boolean isDislikedByMe;
}
