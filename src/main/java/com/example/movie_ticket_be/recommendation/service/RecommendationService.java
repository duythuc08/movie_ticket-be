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

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendationService {

    UserPreferenceRepository userPreferenceRepository;
    PopularityService popularityService;
    RecommendationProperties properties;

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

        return prefs.stream()
                .map(p -> RecommendationItemResponse.builder()
                        .movieId(p.getMovie().getMovieId())
                        .title(p.getMovie().getTitle())
                        .posterUrl(p.getMovie().getPosterUrl())
                        .predictedScore(p.getPredictedScore())
                        .neighborCount(p.getNeighborCount())
                        .source("cf")
                        .build())
                .toList();
    }
}
