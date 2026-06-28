package com.example.movie_ticket_be.recommendation.service.Job1;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.movie.repository.ReviewRepository;
import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import com.example.movie_ticket_be.recommendation.entity.UserActivityLog;
import com.example.movie_ticket_be.recommendation.repository.UserActivityLogRepository;
import com.example.movie_ticket_be.recommendation.repository.UtilityMatrixRepository;

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
    final UserActivityLogRepository userActivityLogRepository;
    final ReviewRepository reviewRepository;
    final UtilityMatrixUpsertService upsertService;
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

            // Pre-load toàn bộ review + activity log của user này 1 lần — tránh N+1
            Map<Long, Reviews> reviewsByMovie = reviewRepository.findApprovalReviewsByUserId(userId)
                    .stream().collect(Collectors.toMap(r -> r.getMovies().getMovieId(), r -> r));
            Map<Long, List<UserActivityLog>> logsByMovie = userActivityLogRepository.findAllByUserId(userId)
                    .stream().collect(Collectors.groupingBy(l -> l.getUserActivityLogId().getMovieId()));

            totalUpserted += upsertService.upsertForUser(userId, interactedMovieIds, reviewsByMovie, logsByMovie);
        }

        log.info("[UtilityMatrixBuilderStep] Hoàn tất — {} ô utility_matrix đã upsert", totalUpserted);
        return RepeatStatus.FINISHED;
    }

}