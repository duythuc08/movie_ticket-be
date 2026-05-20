package com.example.movie_ticket_be.booking.controller;

import com.example.movie_ticket_be.booking.dto.request.FoodRequest;
import com.example.movie_ticket_be.booking.dto.response.FoodResponse;
import com.example.movie_ticket_be.booking.service.AdminFoodService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
