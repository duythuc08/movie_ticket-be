package com.example.movie_ticket_be.recommendation.service.Job2;

import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import com.example.movie_ticket_be.recommendation.repository.UtilityMatrixRepository;
import com.example.movie_ticket_be.recommendation.service.Job1.NormalizationService;
import com.example.movie_ticket_be.recommendation.service.Job1.SimilarityService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)

/**
 * Tính cosine similarity cho mọi cặp user eligible.
 * giữ kết quả TRONG MEMORY qua JobExecutionContext
 * Dữ liệu lưu vào JobExecutionContext gồm 2 phần
 * - "normalizedRows": Map<userId, Map<movieId, r̄>> — hàng đã chuẩn hóa của MỌI eligible user
 * - "topNeighbors": Map<userId, List<Map.Entry<neighborId, sim>>> — top-K neighbor của từng user
 */

public class SimilarityComputationTasklet implements Tasklet {
    public static final String CTX_NORMALIZED_ROWS = "normalizedRows";
    public static final String CTX_TOP_NEIGHBORS = "topNeighbors";

    final UtilityMatrixRepository utilityMatrixRepository;
    final NormalizationService normalizationService;
    final SimilarityService similarityService;
    final RecommendationProperties properties;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int minInteractions = properties.getColdStart().getMinInteractionsThreshold();
        List<String> eligibleUserIds = utilityMatrixRepository.findEligibleUserIds(minInteractions);
        log.info("[SimilarityComputationStep] Chuẩn hóa Y cho {} user eligible...", eligibleUserIds.size());

        //1. Chuẩn hóa Y cho tất cả user eligible
        Map<String,Map<Long, BigDecimal>> normalizeRows = new HashMap<>();
        for (String userId : eligibleUserIds) {
            normalizeRows.put(userId,normalizationService.normalizeUserRow(userId));
        }
        log.info("[SimilarityComputationStep] Tính cosine similarity cho mọi cặp user...");

        // Pre-compute norm 1 lần/user — tránh tính lại N lần trong nested loop
        Map<String, Double> normPerUser = new HashMap<>();
        for (String userId : eligibleUserIds) {
            double norm = Math.sqrt(normalizeRows.get(userId).values().stream()
                    .mapToDouble(v -> v.doubleValue() * v.doubleValue()).sum());
            normPerUser.put(userId, norm);
        }

        //2. Với mỗi user tính similarity với mọi user khác, lấy top-K
        Map<String,List<Map.Entry<String,Double>>> topNeighborsPerUser = new HashMap<>();
        for(String userId : eligibleUserIds){
            Map<Long,BigDecimal> rowA = normalizeRows.get(userId);
            double normA = normPerUser.get(userId);
            Map<String,Double> simWithOthers = new HashMap<>();
            for (String otherId : eligibleUserIds){
                if (otherId.equals(userId)) continue;
                Map<Long,BigDecimal> rowB = normalizeRows.get(otherId);
                double normB = normPerUser.get(otherId);
                Double sim = similarityService.computeCosineSimilarity(rowA, normA, rowB, normB);
                if (sim != null) {
                    simWithOthers.put(otherId,sim);
                }
            }
            topNeighborsPerUser.put(userId,similarityService.findTopNeighbors(simWithOthers));
        }

        //3. Lưu vòa JobExecutionContext để sử dụng cho Job 3
        JobExecution jobExecution = chunkContext.getStepContext().getStepExecution().getJobExecution();
        jobExecution.getExecutionContext().put(CTX_TOP_NEIGHBORS,topNeighborsPerUser);
        jobExecution.getExecutionContext().put(CTX_NORMALIZED_ROWS,normalizeRows);

        log.info("[SimilarityComputationStep] Hoàn tất — đã tính neighbor cho {} user", topNeighborsPerUser.size());
        return RepeatStatus.FINISHED;
    }
}
