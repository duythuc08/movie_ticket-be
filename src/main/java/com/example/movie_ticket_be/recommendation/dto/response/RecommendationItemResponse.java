package com.example.movie_ticket_be.recommendation.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecommendationItemResponse {
    Long movieId;
    String title;
    String posterUrl;
    String description; // For synopsis
    Integer duration;   // For durationText

    BigDecimal predictedScore;
    Integer neighborCount;
    Double averageRating;
    String source;  // "cf" hoặc "cold_start_popularity"
}
