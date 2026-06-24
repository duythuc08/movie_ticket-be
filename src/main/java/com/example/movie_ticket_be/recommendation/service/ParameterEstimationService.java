package com.example.movie_ticket_be.recommendation.service;

import com.example.movie_ticket_be.recommendation.entity.ScoringParam;
import com.example.movie_ticket_be.recommendation.enums.ParamName;
import com.example.movie_ticket_be.recommendation.repository.ScoringParamRepository;
import com.example.movie_ticket_be.recommendation.repository.UserActivityLogRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
/**
 * Ước lượng alpha (trục Tần suất, Hu et al. 2008) và S0 (hệ số bão hòa hàm tanh)
*/
public class ParameterEstimationService {
    final UserActivityLogRepository userActivityLogRepository;
    final ScoringParamRepository scoringParamRepository;
    final ScoringService scoringService;

    @Scheduled(cron = "0 0 3 * * MON")
    @Transactional
    public void runEstimation() {
        log.info("[ParameterEstimation] Bắt đầu ước lượng alpha/S0 từ dữ liệu thật...");
        BigDecimal alpha = estimateAlpha();
        BigDecimal s0 = estimateS0();
        log.info("[ParameterEstimation] Hoàn tất — alpha={}, S0={}", alpha, s0);
    }

    /**
     * alpha = 1 / median(occurrence_count) — chỉ tính trên dòng BOOK_TICKET/
     * SHARE_MOVIE có occurrence_count >= 2
     * Nếu chưa có đủ dữ liệu (danh sách rỗng) -> KHÔNG ghi đè scoring_params,
     * giữ giá trị cũ (nếu có) để tránh đẩy alpha về một số vô nghĩa.
     */
    public BigDecimal estimateAlpha() {
        List<Integer> occurrenceCounts = userActivityLogRepository.findOccurrenceCountsForFrequencyAxis();
        if (occurrenceCounts.isEmpty()) {
            log.warn("[ParameterEstimation] Không có dòng BOOK_TICKET/SHARE_MOVIE với occurrence_count >= 2 "
                    + "- giữ nguyên alpha cũ trong scoring_params (nếu có), không ghi đè.");
            return scoringParamRepository.findById(ParamName.ALPHA)
                    .map(ScoringParam::getParamValue)
                    .orElse(null);
        }
        double medianCount = median(occurrenceCounts.stream().map(Integer::doubleValue).toList());
        BigDecimal alpha = BigDecimal.valueOf(1.0 / medianCount).setScale(6, RoundingMode.HALF_UP);
        upsert(ParamName.ALPHA, alpha, occurrenceCounts.size());
        return alpha;
    }

    /**
     * S0 = median(|S_raw|) — S_raw tính bằng ScoringService.calculateRawScore()
     * cho MỌI cặp (user, movie) CHƯA có review thật (has_explicit = false).
     */
    public BigDecimal estimateS0() {
        List<Object[]> pairs = userActivityLogRepository.findUserMoviePairsWithoutExplicitRating();
        if (pairs.isEmpty()) {
            log.warn("[ParameterEstimation] Không có cặp (user, movie) implicit-only nào "
                    + "- giữ nguyên S0 cũ trong scoring_params (nếu có), không ghi đè.");
            return scoringParamRepository.findById(ParamName.S0)
                    .map(ScoringParam::getParamValue)
                    .orElse(null);
        }
        List<Double> absScores = pairs.stream()
                .map(pair -> {
                    String userId = (String) pair[0];
                    Long movieId = ((Number) pair[1]).longValue();
                    double rawScore = scoringService.calculateRawScore(userId, movieId);
                    return Math.abs(rawScore);
                })
                .toList();
        double medianAbsScore = median(absScores);
        // S0 = 0 sẽ làm tanh(S/S0) chia 0 -> luôn fallback về giá trị dương nhỏ tối thiểu
        BigDecimal s0 = BigDecimal.valueOf(Math.max(medianAbsScore, 0.0001)).setScale(6, RoundingMode.HALF_UP);
        upsert(ParamName.S0, s0, absScores.size());
        return s0;
    }

    void upsert(ParamName paramName, BigDecimal value, int sampleSize){
        ScoringParam param = scoringParamRepository.findById(paramName)
                .orElseGet(() -> ScoringParam.builder().paramName(paramName).build());

        param.setParamValue(value);
        param.setSampleSize(sampleSize);
        param.setComputeAt(LocalDateTime.now());
        scoringParamRepository.save(param);
    }

    // Tính số trung vị của tần suat
    // Dựa vào * Hu, Y., Koren, Y., & Volinsky, C. (2008). Collaborative Filtering for Implicit Feedback Datasets.
    double median(List<Double> values){
        if(values.isEmpty()) return 0.0;
        List<Double> sorted = values.stream().sorted().toList();
        int n = sorted.size();
        if(n % 2 == 1) return sorted.get(n / 2);
        else return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
    }
}
