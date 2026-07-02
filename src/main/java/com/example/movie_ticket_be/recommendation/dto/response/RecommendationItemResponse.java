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
    BigDecimal predictedScore;
    Integer neighborCount;
    String source;  // "cf" hoặc "cold_start_popularity"
}
