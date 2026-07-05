package com.example.movie_ticket_be.recommendation.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.recommendation.dto.response.RecommendationItemResponse;
import com.example.movie_ticket_be.recommendation.service.ParameterEstimationService;
import com.example.movie_ticket_be.recommendation.service.RecommendationService;
import com.example.movie_ticket_be.recommendation.service.RecommendationTrainService;
import com.example.movie_ticket_be.recommendation.service.WeeklyRecommendationEmailService;
import com.example.movie_ticket_be.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/recommendations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecommendationAdminController {

    ParameterEstimationService parameterEstimationService;
    RecommendationTrainService recommendationTrainService;
    RecommendationService recommendationService;
    WeeklyRecommendationEmailService weeklyEmailService;
    UserRepository userRepository;

    @PostMapping("/estimate-parameters")
    public Map<String, BigDecimal> estimateParameters() {
        BigDecimal alpha = parameterEstimationService.estimateAlpha();
        BigDecimal s0 = parameterEstimationService.estimateS0();
        return Map.of("alpha", alpha, "s0", s0);
    }

    /**
     * Trigger train thủ công — dùng khi cần train ngay (demo, seed data mới)
     * mà không chờ scheduler 3AM.
     */
    @PostMapping("/refresh")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Map<String, Object>> refresh() {
        log.info("[RecommendationAdmin] Trigger train thủ công...");
        Map<String, Object> result = recommendationTrainService.triggerTrain();
        return ApiResponse.<Map<String, Object>>builder().result(result).build();
    }

    /**
     * Trigger gửi email gợi ý thủ công — không chờ lịch thứ Hai 8AM.
     */
    @PostMapping("/send-weekly-emails")
    public ApiResponse<Map<String, Object>> sendWeeklyEmails() {
        log.info("[RecommendationAdmin] Trigger gửi mail gợi ý thủ công...");
        Map<String, Integer> result = weeklyEmailService.sendToAllUsers(userRepository.findAllByEnabledTrue());
        return ApiResponse.<Map<String, Object>>builder()
                .message("Gửi email hoàn tất")
                .result(Map.of(
                        "success", result.get("success"),
                        "skip",    result.get("skip"),
                        "fail",    result.get("fail")
                ))
                .build();
    }

    /**
     * Trả top-N gợi ý phim cho user đang đăng nhập.
     * userId lấy từ JWT claim "userId" — không nhận từ query param.
     */
    @GetMapping
    public ApiResponse<List<RecommendationItemResponse>> getMyRecommendations(
            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("userId");
        List<RecommendationItemResponse> result = recommendationService.getRecommendationsForUser(userId);
        return ApiResponse.<List<RecommendationItemResponse>>builder().result(result).build();
    }
}
