package com.example.movie_ticket_be.booking.controller;

import com.example.movie_ticket_be.booking.dto.response.FoodResponse;
import com.example.movie_ticket_be.booking.service.FoodService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FoodController {
    FoodService foodService;

    @GetMapping("/getFoods")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<List<FoodResponse>> getFoods() {
        return ApiResponse.<List<FoodResponse>>builder()
                .result(foodService.getAllFoods())
                .build();
    }

    @GetMapping("/getFood/{id}")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<FoodResponse> getFood(@PathVariable Long id) {
        return ApiResponse.<FoodResponse>builder()
                .result(foodService.getFoodById(id))
                .build();
    }
}
