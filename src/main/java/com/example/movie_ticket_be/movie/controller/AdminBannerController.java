package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.request.BannerRequest;
import com.example.movie_ticket_be.movie.dto.response.BannerResponse;
import com.example.movie_ticket_be.movie.service.AdminBannerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/banners")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminBannerController {
    AdminBannerService adminBannerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> createBanner(@RequestBody @Valid BannerRequest request) {
        return ApiResponse.<BannerResponse>builder()
                .result(adminBannerService.createBanner(request))
                .message("Thêm banner thành công")
                .build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<BannerResponse>> createBanners(@RequestBody List<BannerRequest> requests) {
        return ApiResponse.<List<BannerResponse>>builder()
                .result(adminBannerService.createBanners(requests))
                .build();
    }
}
