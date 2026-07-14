package com.example.movie_ticket_be.recommendation.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminUserRecommendationResponse {
    List<GenreProfile> genreProfile;
    List<RecommendationItemResponse> recommendations;
    boolean usedColdStart;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class GenreProfile {
        String genreName;
        Long likedCount;
        Double weightPct;
    }
}
