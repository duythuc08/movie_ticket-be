package com.example.movie_ticket_be.recommendation.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RecommendationItemResponse {
    Long movieId;
    String title;
    String posterUrl;
    String description;
    Integer duration;
    List<String> genres;

    BigDecimal predictedScore;
    Integer neighborCount;
    Double averageRating;
    String source;
}
