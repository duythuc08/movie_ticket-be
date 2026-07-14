package com.example.movie_ticket_be.recommendation.service;

import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import com.example.movie_ticket_be.recommendation.dto.response.AdminUserRecommendationResponse;
import com.example.movie_ticket_be.recommendation.dto.response.AdminUserRecommendationResponse.GenreProfile;
import com.example.movie_ticket_be.recommendation.dto.response.RecommendationItemResponse;
import com.example.movie_ticket_be.recommendation.entity.UserPreference;
import com.example.movie_ticket_be.recommendation.repository.UserActivityLogRepository;
import com.example.movie_ticket_be.recommendation.repository.UserPreferenceRepository;
import com.example.movie_ticket_be.movie.repository.ReviewRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminRecommendationService {

    UserPreferenceRepository userPreferenceRepository;
    UserActivityLogRepository userActivityLogRepository;
    PopularityService popularityService;
    RecommendationProperties properties;
    ReviewRepository reviewRepository;

    @Transactional(readOnly = true)
    public AdminUserRecommendationResponse getRecommendationsForAdmin(String userId) {
        // Genre profile
        List<GenreProfile> genreProfile = userActivityLogRepository
                .getUserGenreProfile(userId)
                .stream()
                .map(row -> GenreProfile.builder()
                        .genreName(row.getGenreName())
                        .likedCount(row.getLikedCount())
                        .weightPct(row.getWeightPct())
                        .build())
                .toList();

        // Recommendations
        int topN = properties.getPrediction().getTopN();
        List<UserPreference> prefs = userPreferenceRepository
                .findTopByUserIdFetchMovie(userId, PageRequest.of(0, topN));

        boolean usedColdStart;
        List<RecommendationItemResponse> recommendations;

        if (prefs.isEmpty()) {
            recommendations = popularityService.getTopMoviesForUser(userId);
            usedColdStart = true;
        } else {
            List<Long> movieIds = prefs.stream().map(p -> p.getMovie().getMovieId()).toList();
            Map<Long, Double> avgRatings = reviewRepository.findAvgRatingByMovieIds(movieIds).stream()
                    .collect(Collectors.toMap(
                            row -> ((Number) row[0]).longValue(),
                            row -> ((Number) row[1]).doubleValue()
                    ));
            recommendations = prefs.stream()
                    .map(p -> RecommendationItemResponse.builder()
                            .movieId(p.getMovie().getMovieId())
                            .title(p.getMovie().getTitle())
                            .posterUrl(p.getMovie().getPosterUrl())
                            .description(p.getMovie().getDescription())
                            .duration(p.getMovie().getDuration())
                            .genres(p.getMovie().getGenre().stream()
                                    .map(g -> g.getName())
                                    .sorted()
                                    .toList())
                            .predictedScore(p.getPredictedScore())
                            .neighborCount(p.getNeighborCount())
                            .averageRating(avgRatings.getOrDefault(p.getMovie().getMovieId(), 0.0))
                            .source(p.getSource())
                            .build())
                    .toList();
            usedColdStart = false;
        }

        return AdminUserRecommendationResponse.builder()
                .genreProfile(genreProfile)
                .recommendations(recommendations)
                .usedColdStart(usedColdStart)
                .build();
    }
}
