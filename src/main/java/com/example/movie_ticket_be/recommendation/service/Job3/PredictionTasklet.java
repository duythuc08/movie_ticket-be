package com.example.movie_ticket_be.recommendation.service.Job3;

import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import com.example.movie_ticket_be.recommendation.repository.UserPreferenceRepository;
import com.example.movie_ticket_be.recommendation.repository.UtilityMatrixRepository;
import com.example.movie_ticket_be.recommendation.service.Job2.PredictionService;
import com.example.movie_ticket_be.recommendation.service.Job2.SimilarityComputationTasklet;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

/**
 * Step 3 — PredictionStep (Mục VI/Bước 5 batch, DAC_TA).
 * Lấy lại normalizedRows + topNeighbors mà Step 2 đã tính sẵn (qua
 * JobExecutionContext) — không tính lại từ đầu. Với mỗi user eligible,
 * gọi PredictionService.predictTopNForUser() để ra top N phim, rồi
 * UPSERT kết quả vào user_preference.
 */
public class PredictionTasklet implements Tasklet {
    UtilityMatrixRepository utilityMatrixRepository;
    UserPreferenceRepository userPreferenceRepository;
    PredictionService predictionService;
    RecommendationProperties properties;

    @SuppressWarnings("unchecked")
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int topN = properties.getPrediction().getTopN();
        JobExecution jobExecution = contribution.getStepExecution().getJobExecution();
        Map<String, Map<Long, BigDecimal>> normalizedRows = (Map<String, Map<Long, BigDecimal>>) jobExecution.getExecutionContext().get(SimilarityComputationTasklet.CTX_NORMALIZED_ROWS);
        Map<String, List<Map.Entry<String, Double>>> topNeighborsPerUser = (Map<String, List<Map.Entry<String, Double>>>) jobExecution.getExecutionContext().get(SimilarityComputationTasklet.CTX_TOP_NEIGHBORS);

        if (normalizedRows == null || topNeighborsPerUser == null) {
            log.error("[PredictionStep] Không tìm thấy dữ liệu từ Step 2 trong JobExecutionContext "
                    + "- kiểm tra lại thứ tự Step trong Job (SimilarityComputationStep phải chạy TRƯỚC PredictionStep)");
            return RepeatStatus.FINISHED;
        }

        int minInteractions = properties.getColdStart().getMinInteractionsThreshold();
        List<String> eligibleUserIds = utilityMatrixRepository.findEligibleUserIds(minInteractions);
        log.info("[PredictionStep] Predict top-{} cho {} user...", topN, eligibleUserIds.size());

        int totalUsersUpdated = 0;
        for (String userId : eligibleUserIds) {
            List<Map.Entry<String, Double>> neighbors = topNeighborsPerUser.getOrDefault(userId, List.of());
            if (neighbors.isEmpty()) {
                log.warn("[PredictionStep] Không tìm thấy neighbor cho userId: {} - bỏ qua", userId);
                continue;
            }
            List<PredictionService.PredictedMovie> predicted = predictionService.predictTopNForUser(userId, neighbors, normalizedRows, topN);

            // Xóa hết dòng cũ trước khi upsert lượt mới - tránh sót phim không
            // còn trong candidate set lượt này (xem ghi chú trong UserPreferenceRepository).
            userPreferenceRepository.deleteAllByUserId(userId);
            for (PredictionService.PredictedMovie movie : predicted) {
                userPreferenceRepository.upsert(userId, movie.movieId(), movie.score(), movie.neighborCount());
            }
            totalUsersUpdated++;
        }
        log.info("[PredictionStep] Hoàn tất — đã cập nhật user_preference cho {} user", totalUsersUpdated);
        return RepeatStatus.FINISHED;
    }
}
