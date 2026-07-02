package com.example.movie_ticket_be.recommendation.schedule;

import com.example.movie_ticket_be.recommendation.service.RecommendationTrainService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendationTrainScheduler {

    RecommendationTrainService recommendationTrainService;

    /**
     * 3AM mỗi ngày: gọi Python FastAPI /api/train để train CF cho toàn bộ user
     * và ghi kết quả vào bảng user_preference. Nếu Python service down, chỉ log
     * cảnh báo — không crash Spring Boot, user vẫn đọc được dữ liệu cũ.
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void scheduledTrain() {
        log.info("[RecommendationScheduler] Bắt đầu batch CF train (3AM)...");
        try {
            Map<String, Object> result = recommendationTrainService.triggerTrain();
            log.info("[RecommendationScheduler] Hoàn tất — nUsersProcessed={}, nPredictionsWritten={}, batchElapsedSeconds={}",
                    result.get("nUsersProcessed"),
                    result.get("nPredictionsWritten"),
                    result.get("batchElapsedSeconds"));
        } catch (Exception e) {
            log.error("[RecommendationScheduler] Lỗi khi gọi Python train service: {} — bỏ qua, dữ liệu cũ vẫn dùng được",
                    e.getMessage());
        }
    }
}
