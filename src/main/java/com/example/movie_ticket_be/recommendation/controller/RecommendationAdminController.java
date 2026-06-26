package com.example.movie_ticket_be.recommendation.controller;

import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import com.example.movie_ticket_be.recommendation.schedule.RecommendationJobScheduler;
import com.example.movie_ticket_be.recommendation.service.ParameterEstimationService;
import com.example.movie_ticket_be.recommendation.service.ScoringService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendationAdminController {
    final ParameterEstimationService parameterEstimationService;
    final RecommendationJobScheduler recommendationJobScheduler;

    //Trigger thủ công ParameterEstimationService
    @PostMapping("/estimate-parameters")
    public Map<String, BigDecimal> estimateParameters() {
        BigDecimal alpha = parameterEstimationService.estimateAlpha();
        BigDecimal s0 = parameterEstimationService.estimateS0();
        return Map.of("alpha", alpha, "s0", s0);
    }

    @PostMapping("/refresh")
    public Map<String, String> refresh() {
        recommendationJobScheduler.triggerJob();
        return Map.of("status", "Job recommendationRefreshJob đã được kích hoạt — xem log để theo dõi tiến trình");
    }
}
