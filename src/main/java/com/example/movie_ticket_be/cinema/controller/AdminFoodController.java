package com.example.movie_ticket_be.cinema.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.cinema.dto.request.FoodRequest;
import com.example.movie_ticket_be.cinema.dto.response.FoodResponse;
import com.example.movie_ticket_be.cinema.entity.Foods;
import com.example.movie_ticket_be.cinema.service.AdminFoodService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/admin/foods")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminFoodController {
    AdminFoodService adminFoodService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FoodResponse> createFood(@RequestParam Long cinemaId, @RequestBody FoodRequest request) {
        return ApiResponse.<FoodResponse>builder()
                .result(adminFoodService.createFood(cinemaId, request))
                .message("Thêm food thành công")
                .build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FoodResponse>> createFoods(@RequestParam Long cinemaId, @RequestBody List<FoodRequest> requests) {
        return ApiResponse.<List<FoodResponse>>builder()
                .result(adminFoodService.createFoods(cinemaId, requests))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<FoodResponse>> getFoods(
            @RequestParam Long cinemaId,
            @Parameter(name = "filter", required = false) @Filter Specification<Foods> spec,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<FoodResponse>>builder()
                .result(adminFoodService.getFoods(cinemaId, spec, pageable))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FoodResponse> updateFood(@PathVariable long id, @RequestBody FoodRequest request) {
        return ApiResponse.<FoodResponse>builder()
                .result(adminFoodService.updateFood(id, request))
                .message("Cập nhật food thành công")
                .build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activate(@PathVariable long id) {
        adminFoodService.changeStatus(id, EntityStatus.ACTIVE);
        return ApiResponse.<Void>builder().message("Kích hoạt food thành công").build();
    }

    @PutMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> inactivate(@PathVariable long id) {
        adminFoodService.changeStatus(id, EntityStatus.INACTIVE);
        return ApiResponse.<Void>builder().message("Vô hiệu hóa food thành công").build();
    }
}
