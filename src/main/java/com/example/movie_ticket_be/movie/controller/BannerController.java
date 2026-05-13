package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.request.BannerRequest;
import com.example.movie_ticket_be.movie.dto.response.BannerResponse;
import com.example.movie_ticket_be.movie.service.BannerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class BannerController {
    BannerService bannerService;


    @PostMapping
    ApiResponse<BannerResponse> createBanner(@RequestBody @Valid BannerRequest bannerRequest){
        return ApiResponse.<BannerResponse>builder()
                .result(bannerService.createBanner(bannerRequest))
                .message("Thành công")
                .build();
    }


    @PostMapping("/postBanners")
    public ApiResponse<List<BannerResponse>> createBanners(@RequestBody List<BannerRequest> requests) {
        return ApiResponse.<List<BannerResponse>>builder()
                .result(bannerService.createBanners(requests))
                .build();
    }

    @GetMapping("/getBanners")
    ApiResponse<List<BannerResponse>> listBanners(){
        return ApiResponse.<List<BannerResponse>>builder()
                .result(bannerService.getBanners())
                .build();
    }

    @GetMapping("/getBanners_active")
    ApiResponse<List<BannerResponse>> listBannersActive(){
        return ApiResponse.<List<BannerResponse>>builder()
                .result(bannerService.getBannersByActive())
                .build();
    }

    @GetMapping("/getBannerByMovieId/{movieId}")
    ApiResponse<BannerResponse> getBannerByMovieId(@PathVariable Long movieId) {
        return ApiResponse.<BannerResponse>builder()
                .result(bannerService.getBannerByMovieId(movieId))
                .build();
    }
}
