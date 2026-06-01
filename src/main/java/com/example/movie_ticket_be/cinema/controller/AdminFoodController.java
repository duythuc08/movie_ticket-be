package com.example.movie_ticket_be.cinema.controller;

import com.example.movie_ticket_be.cinema.dto.request.FoodRequest;
import com.example.movie_ticket_be.cinema.dto.response.FoodResponse;
import com.example.movie_ticket_be.cinema.entity.Foods;
import com.example.movie_ticket_be.cinema.service.AdminFoodService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/foods")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminFoodController {
    AdminFoodService adminFoodService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<FoodResponse> createFood(@RequestBody FoodRequest request) {
        return ApiResponse.<FoodResponse>builder()
                .result(adminFoodService.createFood(request))
                .message("Thêm food thành công")
                .build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<FoodResponse>> createFoods(@RequestBody List<FoodRequest> requests) {
        return ApiResponse.<List<FoodResponse>>builder()
                .result(adminFoodService.createFoods(requests))
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<FoodResponse>> getFoods(Specification<Foods> specification, Pageable pageable) {
        return ApiResponse.<Page<FoodResponse>>builder()
                .result(adminFoodService.getFoods(specification, pageable))
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
