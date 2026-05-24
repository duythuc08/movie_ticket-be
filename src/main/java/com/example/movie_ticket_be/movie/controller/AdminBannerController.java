package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.request.BannerRequest;
import com.example.movie_ticket_be.movie.dto.response.BannerResponse;
import com.example.movie_ticket_be.movie.entity.Banner;
import com.example.movie_ticket_be.movie.service.AdminBannerService;
import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
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

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BannerResponse> updateBanner(@PathVariable Long id, @RequestBody @Valid BannerRequest request) {
        return ApiResponse.<BannerResponse>builder()
                .result(adminBannerService.updateBanner(id, request))
                .message("Cập nhật banner thành công")
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteBanner(@PathVariable Long id) {
        adminBannerService.deleteBanner(id);
        return ApiResponse.<Void>builder()
                .message("Xóa banner thành công")
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<BannerResponse>> getAllBanners(
            @Parameter(name = "filter", required = false) @Filter Specification<Banner> spec,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<BannerResponse>>builder()
                .result(adminBannerService.getAllBanners(spec, pageable))
                .message("Lấy danh sách banner thành công")
                .build();
    }
}
