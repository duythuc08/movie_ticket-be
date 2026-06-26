package com.example.movie_ticket_be.recommendation.config;

import com.example.movie_ticket_be.recommendation.service.Job1.UtilityMatrixBuilderTasklet;
import com.example.movie_ticket_be.recommendation.service.Job2.SimilarityComputationTasklet;
import com.example.movie_ticket_be.recommendation.service.Job3.PredictionTasklet;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Job tổng 3 Job chạy TUẦN TỰ, bắt buộc đúng thứ tự vì Step 2/3 phụ thuộc dữ liệu
 *   buildUtilityMatrixStep -> computeSimilarityStep -> predictTopNStep
 * Lịch chạy: hàng đêm 3AM — độc lập với ParameterEstimationService (chạy hàng tuần, thứ Hai 3AM).
 * Có thể trigger thủ công qua endpoint admin
 */
@Configuration
@RequiredArgsConstructor
public class RecommendationBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final UtilityMatrixBuilderTasklet utilityMatrixBuilderTasklet;
    private final SimilarityComputationTasklet similarityComputationTasklet;
    private final PredictionTasklet predictionTasklet;

    @Bean
    public Step buildUtilityMatrixStep() {
        return new StepBuilder("buildUtilityMatrixStep", jobRepository)
                .tasklet(utilityMatrixBuilderTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step computeSimilarityStep() {
        return new StepBuilder("computeSimilarityStep", jobRepository)
                .tasklet(similarityComputationTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step predictTopNStep() {
        return new StepBuilder("predictTopNStep", jobRepository)
                .tasklet(predictionTasklet, transactionManager)
                .build();
    }

    @Bean
    public Job recommendationRefreshJob() {
        return new JobBuilder("recommendationRefreshJob", jobRepository)
                .start(buildUtilityMatrixStep())
                .next(computeSimilarityStep())
                .next(predictTopNStep())
                .build();
    }
}

