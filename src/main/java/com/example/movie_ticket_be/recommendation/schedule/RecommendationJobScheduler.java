package com.example.movie_ticket_be.recommendation.schedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Chạy recommendationRefreshJob hàng đêm 3AM . JobParameters
 * luôn kèm timestamp hiện tại để Spring Batch coi đây là 1 lần chạy MỚI —
 * nếu không có tham số phân biệt, JobRepository sẽ từ chối chạy lại Job đã
 * COMPLETED với cùng JobParameters (mặc định Spring Batch không cho chạy lại
 * job giống y hệt 2 lần).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RecommendationJobScheduler {

    private final JobLauncher jobLauncher;
    private final Job recommendationRefreshJob;

    @Scheduled(cron = "0 0 3 * * *")
    public void runNightlyJob() {
        triggerJob();
    }

    /** Dùng lại cho cả lịch hàng đêm và endpoint trigger thủ công (RecommendationAdminController). */
    public void triggerJob() {
        try {
            var params = new JobParametersBuilder()
                    .addLong("triggeredAt", Instant.now().toEpochMilli())
                    .toJobParameters();
            jobLauncher.run(recommendationRefreshJob, params);
        } catch (Exception e) {
            log.error("[RecommendationJobScheduler] Job recommendationRefreshJob chạy thất bại", e);
        }
    }
}


