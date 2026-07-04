package com.example.movie_ticket_be.recommendation.service;

import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import com.example.movie_ticket_be.recommendation.dto.response.RecommendationItemResponse;
import com.example.movie_ticket_be.recommendation.entity.UserPreference;
import com.example.movie_ticket_be.recommendation.repository.UserPreferenceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import com.example.movie_ticket_be.movie.repository.ReviewRepository;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendationService {

    UserPreferenceRepository userPreferenceRepository;
    PopularityService popularityService;
    RecommendationProperties properties;
    ReviewRepository reviewRepository;

    /**
     * Đọc top-N từ user_preference (đã được Python ghi sẵn lúc train 3AM).
     * Nếu không có dòng nào cho userId này (cold-start hoặc batch chưa chạy)
     * -> fallback sang PopularityService.
     */
    @Transactional(readOnly = true)
    public List<RecommendationItemResponse> getRecommendationsForUser(String userId) {
        int topN = properties.getPrediction().getTopN();
        List<UserPreference> prefs = userPreferenceRepository
                .findTopByUserIdFetchMovie(userId, PageRequest.of(0, topN));

        if (prefs.isEmpty()) {
            log.debug("[Recommendation] userId={} không có dữ liệu trong user_preference -> fallback Popularity", userId);
            return popularityService.getTopMoviesForUser(userId);
        }

        List<Long> movieIds = prefs.stream().map(p -> p.getMovie().getMovieId()).toList();
        Map<Long, Double> avgRatings = reviewRepository.findAvgRatingByMovieIds(movieIds).stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).doubleValue()
                ));

        return prefs.stream()
                .map(p -> RecommendationItemResponse.builder()
                        .movieId(p.getMovie().getMovieId())
                        .title(p.getMovie().getTitle())
                        .posterUrl(p.getMovie().getPosterUrl())
                        .description(p.getMovie().getDescription())
                        .duration(p.getMovie().getDuration())
                        .predictedScore(p.getPredictedScore())
                        .neighborCount(p.getNeighborCount())
                        .averageRating(avgRatings.getOrDefault(p.getMovie().getMovieId(), 0.0))
                        .source(p.getSource())
                        .build())
                .toList();
    }
}
