package com.example.movie_ticket_be.recommendation.service.Step1;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.recommendation.entity.UtilityMatrix;
import com.example.movie_ticket_be.recommendation.repository.UtilityMatrixRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

/**
 * Chuẩn hóa hàng (row) của Utility Matrix Y theo trung bình từng user
 * theo công thức: Y_normalized(u, i) = Y(u, i) - mean(Y(u, *))
 */

public class NormalizationService {
    final UtilityMatrixRepository utilityMatrixRepository;

    /** ȳ_u — trung bình toàn bộ y_score hiện có của user (mọi phim, không lọc has_explicit). */
    public BigDecimal computeUserMean(String userId) {
        List<UtilityMatrix> rows = utilityMatrixRepository.findAllByUserId(userId);
        if (rows.isEmpty()) {
            log.warn("[NormalizationService] No rows found for userId: {}", userId);
            return BigDecimal.ZERO;
        }

        BigDecimal sum = rows.stream()
                .map(UtilityMatrix::getYScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return sum.divide(BigDecimal.valueOf(rows.size()),6 , BigDecimal.ROUND_HALF_UP);
    }

    /**
     * normalizeUserRow(userId) — trả map {movieId: r̄_{u,i}}, dùng làm input
     * trực tiếp cho SimilarityService.computeCosineSimilarity().
     */
    public Map<Long, BigDecimal> normalizeUserRow(String userId) {
        List<UtilityMatrix> rows = utilityMatrixRepository.findAllByUserId(userId);
        BigDecimal mean = computeUserMean(userId);

        Map<Long, BigDecimal> normalized = new HashMap<>();
        for (UtilityMatrix row : rows) {
            BigDecimal centered = row.getYScore().subtract(mean);
            normalized.put(row.getMatrixId().getMovieId(), centered);
        }
        return normalized;
    }
}