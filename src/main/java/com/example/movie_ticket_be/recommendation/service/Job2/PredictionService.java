package com.example.movie_ticket_be.recommendation.service.Job2;

import com.example.movie_ticket_be.recommendation.repository.CandidateMovieRepository;
import com.example.movie_ticket_be.recommendation.repository.UtilityMatrixRepository;
import com.example.movie_ticket_be.recommendation.service.Job1.NormalizationService;
import com.example.movie_ticket_be.recommendation.service.Job1.SimilarityService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

/**
 * Dự đoán điểm ŷ(u,i) chỉ tính cho những
 * phim nằm trong danh sách candidate của user
 * Lưu ý: dữ liệu của NEIGHBOR thì lấy Y đầy đủ (cả implicit + explicit),
 * KHÔNG lọc gì cả — vì neighbor chỉ dùng để "góp ý" tính điểm dự đoán,
 * không phải đối tượng đang xét candidate set. Việc lọc candidate set
 * (excluded_movies) CHỈ áp dụng cho user u — phim nào của neighbor cũng
 * dùng được để tính sim/predict, kể cả phim mà neighbor chỉ mới xem trailer.
 */
public class PredictionService {
    final NormalizationService normalizationService;
    final SimilarityService similarityService;
    final UtilityMatrixRepository utilityMatrixRepository;
    final CandidateMovieRepository candidateMovieRepository;

    public record PredictedMovie(Long movieId, BigDecimal score, int neighborCount) { }

    public Optional<PredictedMovie> predict(String userId, Long movieId,
                                            List<Map.Entry<String, Double>> neighbors,
                                            Map<String, Map<Long,BigDecimal>> neighborNormalizedRows,
                                            BigDecimal userMean) {
        double weightedSum  = 0.0;
        double simAbsSum = 0.0;
        int contributingNeighbors = 0;

        for (Map.Entry<String, Double> neighbor : neighbors) {
            String neighborId = neighbor.getKey();
            Double sim = neighbor.getValue();

            Map<Long,BigDecimal> neighborRow = neighborNormalizedRows.get(neighborId);
            if (neighborRow == null || !neighborRow.containsKey(movieId)) {
                continue; // neighbor chưa đánh giá phim này
            }

            weightedSum += sim * neighborRow.get(movieId).doubleValue();
            simAbsSum += Math.abs(sim);
            contributingNeighbors++;
        }

        if(contributingNeighbors == 0 || simAbsSum == 0.0) {
            return Optional.empty(); // không có neighbor nào đánh giá phim này
        }

        double predicted = userMean.doubleValue() + (weightedSum / simAbsSum);
        BigDecimal score = BigDecimal.valueOf(predicted).setScale(4, RoundingMode.HALF_UP);
        return Optional.of(new PredictedMovie(movieId, score, contributingNeighbors));
    }

    /**
     * Lấy danh sách phim có thể gợi ý cho user (Mục 2.6).
     * = Phim NOW_SHOWING/COMING_SOON, trừ phim user đã rating thật hoặc đã đặt vé thành công (PAID).
     */
    public List<Long> getCandidateMovies(String userId) {
        return candidateMovieRepository.findCandidateMovieIds(userId);
    }


    /**
     * Lấy candidate movies, tính điểm ŷ cho từng phim, sort giảm dần, lấy top N.
     * neighborNormalizedRows phải truyền sẵn từ ngoài (đã tính 1 lần cho hết
     * neighbor ở PredictionStep)
    */
    public List<PredictedMovie> predictTopNForUser(String userId,
                                                   List<Map.Entry<String, Double>> neighbors,
                                                   Map<String, Map<Long,BigDecimal>> neighborNormalizedRows,
                                                   int topN) {
        BigDecimal userMean = normalizationService.computeUserMean(userId);
        List<Long> candidates = getCandidateMovies(userId);
        return candidates.stream()
                .map(movieId -> predict(userId, movieId, neighbors, neighborNormalizedRows, userMean))
                .flatMap(Optional::stream)
                .sorted((a, b) -> b.score().compareTo(a.score())) // sort giảm dần theo score
                .limit(topN)
                .toList();
    }
}
