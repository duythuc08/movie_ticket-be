package com.example.movie_ticket_be.recommendation.service.Job1;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import com.example.movie_ticket_be.recommendation.repository.UtilityMatrixRepository;
import com.example.movie_ticket_be.recommendation.service.ScoringService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Component
/**
 * Đọc tất cả user eligible (K_u >= ngưỡng cold-start), với MỖI (user, movie)
 * mà user có ít nhất 1 dòng activity_log hoặc 1 review -> gọi
 * ScoringService.calculateY() -> UPSERT vào utility_matrix. Tính cho TẤT CẢ
 * phim user có tương tác (kể cả phim đã đặt vé/rating)
 */

public class UtilityMatrixBuilderTasklet implements Tasklet {
    final UtilityMatrixRepository utilityMatrixRepository;
    final ScoringService scoringService;
    final RecommendationProperties properties;

    /**
     * Lấy danh sách (user, movie) cần build Y. chỉ build Y cho cặp đã
     * có DẤU HIỆU tương tác (review hoặc activity log), đúng định nghĩa Y
     */
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        int minInteractions = properties.getColdStart().getMinInteractionsThreshold();
        List<String> eligibleUserIds = utilityMatrixRepository.findEligibleUserIds(minInteractions);

        log.info("[UtilityMatrixBuilderStep] {} user eligible (K_u >= {})",
                eligibleUserIds.size(), minInteractions);

        int totalUpserted = 0;
        for (String userId : eligibleUserIds) {
            List<Long> interactedMovieIds = utilityMatrixRepository.findInteractedMovieIds(userId);
            for (Long movieId : interactedMovieIds) {
                BigDecimal y = scoringService.calculateY(userId, movieId);
                Boolean hasExplicit = scoringService.hasExplicitRating(userId, movieId);
                utilityMatrixRepository.upsert(userId, movieId, y, hasExplicit);
                totalUpserted++;
            }
        }

        log.info("[UtilityMatrixBuilderStep] Hoàn tất — {} ô utility_matrix đã upsert", totalUpserted);
        return RepeatStatus.FINISHED;
    }

}