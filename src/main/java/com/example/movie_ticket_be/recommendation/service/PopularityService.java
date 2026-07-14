package com.example.movie_ticket_be.recommendation.service;

import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.repository.ReviewRepository;
import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import com.example.movie_ticket_be.recommendation.dto.response.RecommendationItemResponse;
import com.example.movie_ticket_be.recommendation.repository.CandidateMovieRepository;
import com.example.movie_ticket_be.recommendation.repository.UserActivityLogRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PopularityService {

    CandidateMovieRepository candidateMovieRepository;
    ReviewRepository reviewRepository;
    UserActivityLogRepository userActivityLogRepository;
    RecommendationProperties properties;

    /**
     * Popularity fallback cho user: lấy candidate movies của user đó,
     * tính Score_Popularity = alpha*Norm_Rating + (1-alpha)*Norm_Tickets,
     * trả top-N theo config.
     */
    @Transactional(readOnly = true)
    public List<RecommendationItemResponse> getTopMoviesForUser(String userId) {
        List<Long> candidateIds = candidateMovieRepository.findCandidateMovieIds(userId);
        if (candidateIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Double> ratingMap = new HashMap<>();
        for (Object[] row : reviewRepository.findAvgRatingByMovieIds(candidateIds)) {
            ratingMap.put(((Number) row[0]).longValue(), ((Number) row[1]).doubleValue());
        }

        Map<Long, Long> ticketMap = new HashMap<>();
        for (Object[] row : userActivityLogRepository.findBookTicketCountByMovieIds(candidateIds)) {
            ticketMap.put(((Number) row[0]).longValue(), ((Number) row[1]).longValue());
        }

        List<Double> allRatings = new ArrayList<>(ratingMap.values());
        List<Double> allTickets = ticketMap.values().stream().map(v -> (double) v).toList();

        double alpha = properties.getColdStart().getPopularityAlpha();
        Map<Long, Double> scores = new HashMap<>();
        for (Long movieId : candidateIds) {
            double normRating = normalize(ratingMap.getOrDefault(movieId, 0.0), allRatings);
            double normTickets = normalize((double) ticketMap.getOrDefault(movieId, 0L), allTickets);
            scores.put(movieId, alpha * normRating + (1 - alpha) * normTickets);
        }

        int topN = properties.getPrediction().getTopN();
        List<Long> topIds = scores.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(topN)
                .map(e -> e.getKey())
                .toList();

        List<Movies> movies = new ArrayList<>(candidateMovieRepository.findAllByIdWithGenres(topIds));
        Map<Long, Movies> movieMap = new HashMap<>();
        movies.forEach(m -> movieMap.put(m.getMovieId(), m));

        return topIds.stream()
                .filter(movieMap::containsKey)
                .<RecommendationItemResponse>map(id -> {
                    Movies m = movieMap.get(id);
                    return RecommendationItemResponse.builder()
                            .movieId(m.getMovieId())
                            .title(m.getTitle())
                            .posterUrl(m.getPosterUrl())
                            .description(m.getDescription())
                            .duration(m.getDuration())
                            .genres(m.getGenre() == null ? List.of() :
                                    m.getGenre().stream().map(g -> g.getName()).sorted().toList())
                            .predictedScore(BigDecimal.valueOf(scores.get(id)))
                            .neighborCount(0)
                            .averageRating(ratingMap.getOrDefault(id, 0.0))
                            .source("cold_start_popularity")
                            .build();
                })
                .toList();
    }

    private double normalize(double value, List<Double> values) {
        if (values.isEmpty()) return 0.0;
        double lo = values.stream().mapToDouble(d -> d).min().orElse(0.0);
        double hi = values.stream().mapToDouble(d -> d).max().orElse(0.0);
        if (hi == lo) return 0.5;
        return (value - lo) / (hi - lo);
    }
}
