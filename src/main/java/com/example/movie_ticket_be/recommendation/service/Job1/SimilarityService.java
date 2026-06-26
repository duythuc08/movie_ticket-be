package com.example.movie_ticket_be.recommendation.service.Job1;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

/**
 * Cosine Similarity giữa các user trên hàng Utility Matrix đã chuẩn hóa
 * Nhận vào map {movieId: r̄} đã tính sẵn bởi NormalizationService
 */
public class SimilarityService {
    final RecommendationProperties properties;

    /**
     * Tính độ tương đồng của 2 user A, B theo công thức cosine similarity:
     * sim(u, v) = (Σ r̄_{u,i} × r̄_{v,i}) / (||r̄_u|| × ||r̄_v||)
     * — chỉ tính trên tập GIAO của 2 user (Σ chỉ chạy qua movie cả 2 cùng có),
     */
    public Double computeCosineSimilarity(Map<Long, BigDecimal> userA, Map<Long, BigDecimal> userB) {
        return computeCosineSimilarity(userA, norm(userA.values()), userB, norm(userB.values()));
    }

    /** Overload nhận norm đã pre-compute — dùng trong nested loop để tránh tính norm lại N lần/user. */
    public Double computeCosineSimilarity(Map<Long, BigDecimal> userA, double normA,
                                          Map<Long, BigDecimal> userB, double normB) {
        if (normA == 0.0 || normB == 0.0) return null;

        int coRated = 0;
        double dotProduct = 0.0;
        for (Long movieId : userA.keySet()) {
            BigDecimal bVal = userB.get(movieId);
            if (bVal == null) continue;
            dotProduct += userA.get(movieId).doubleValue() * bVal.doubleValue();
            coRated++;
        }

        if (coRated < properties.getCf().getMinCoRatedItems()) return null;

        return dotProduct / (normA * normB);
    }

    private double norm(Collection<BigDecimal> values) {
        double sumSquares = values.stream()
                .mapToDouble(v -> v.doubleValue() * v.doubleValue())
                .sum();
        return Math.sqrt(sumSquares);
    }

    /**
     * findTopNeighbors — sắp giảm dần theo sim, lọc sim > minSimilarity, lấy top-K (sl neighbor tương đồng với top-K phim đề xuất)
     * candidates: map {neighborUserId: sim(u, neighbor)} đã tính sẵn cho mọi user khác.
     */
    public List<Map.Entry<String, Double>> findTopNeighbors(Map<String, Double> candidates) {
        return candidates.entrySet().stream()
                .filter(e -> e.getValue() != null && e.getValue() > properties.getCf().getMinSimilarity())
                .sorted(Comparator.comparingDouble((Map.Entry<String, Double> e) -> e.getValue()).reversed())
                .limit(properties.getCf().getTopK())
                .toList();
    }

}