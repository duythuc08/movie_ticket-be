package com.example.movie_ticket_be.booking.controller;

import com.example.movie_ticket_be.booking.dto.request.FoodRequest;
import com.example.movie_ticket_be.booking.dto.response.FoodResponse;
import com.example.movie_ticket_be.booking.service.FoodService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/foods")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class FoodController {
    FoodService foodService;

    public ApiResponse<FoodResponse> postFood(@RequestBody FoodRequest foodRequest){
        return ApiResponse.<FoodResponse>builder()
                .result(foodService.createFood(foodRequest))
                .build();
    }

    @PostMapping("/postFoods")
    public ApiResponse<List<FoodResponse>> postFoods(@RequestBody List<FoodRequest> foodRequest){
        return ApiResponse.<List<FoodResponse>>builder()
                .result(foodService.createFoods(foodRequest))
                .build();
    }

    @GetMapping("/getFoods")
    public ApiResponse<List<FoodResponse>> getFoods(){
        return ApiResponse.<List<FoodResponse>>builder()
                .result(foodService.getAllFoods())
                .build();
    }

    @GetMapping("/getFood/{id}")
    public  ApiResponse<FoodResponse> getFood(@PathVariable Long id){
        return ApiResponse.<FoodResponse>builder()
                .result(foodService.getFoodById(id))
                .build();
    }
}
