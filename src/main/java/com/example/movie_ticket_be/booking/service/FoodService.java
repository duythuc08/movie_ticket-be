package com.example.movie_ticket_be.booking.service;

import com.example.movie_ticket_be.booking.dto.request.FoodRequest;
import com.example.movie_ticket_be.booking.dto.response.FoodResponse;
import com.example.movie_ticket_be.booking.entity.Foods;
import com.example.movie_ticket_be.booking.mapper.FoodMapper;
import com.example.movie_ticket_be.booking.repository.FoodRepository;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class FoodService {
    FoodRepository foodRepository;
    FoodMapper foodMapper;

    @PreAuthorize("hasRole('ADMIN')")
    public FoodResponse createFood(FoodRequest foodRequest) {
        if (foodRepository.existsByName(foodRequest.getName())) {
            throw new AppException(ErrorCode.FOOD_EXISTED);
        }
        Foods foods = foodMapper.toFoods(foodRequest);
        return foodMapper.toFoodResponse(foodRepository.save(foods));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<FoodResponse> createFoods(List<FoodRequest> foodRequests) {
        return foodRequests.stream()
                .map(this::createFood)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    public List<FoodResponse> getAllFoods() {
        return foodRepository.findAll()
                .stream()
                .map(foodMapper::toFoodResponse)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    public FoodResponse getFoodById(Long foodId) {
        Foods foods = foodRepository.findByFoodId(foodId)
                .orElseThrow(() ->  new AppException(ErrorCode.FOOD_NOT_FOUND));
        return foodMapper.toFoodResponse(foods);
    }
}
