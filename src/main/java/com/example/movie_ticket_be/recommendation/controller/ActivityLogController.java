package com.example.movie_ticket_be.recommendation.controller;

import com.example.movie_ticket_be.recommendation.dto.request.ActivityLogRequest;
import com.example.movie_ticket_be.recommendation.service.UserActivityLogService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/activity")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ActivityLogController {

    UserActivityLogService userActivityLogService;

    @PatchMapping
    public ApiResponse<Void> logActivity(@RequestBody ActivityLogRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        userActivityLogService.logFromFrontend(request, jwt.getClaimAsString("userId"));
        return ApiResponse.<Void>builder().build();
    }
}