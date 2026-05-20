package com.example.movie_ticket_be.booking.service;

import com.example.movie_ticket_be.booking.dto.request.FoodRequest;
import com.example.movie_ticket_be.booking.dto.response.FoodResponse;
import com.example.movie_ticket_be.booking.entity.Foods;
import com.example.movie_ticket_be.booking.mapper.FoodMapper;
import com.example.movie_ticket_be.booking.repository.FoodRepository;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminFoodService {
    FoodRepository foodRepository;
    FoodMapper foodMapper;

    public FoodResponse createFood(FoodRequest request) {
        if (foodRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.FOOD_EXISTED);
        }
        Foods foods = foodMapper.toFoods(request);
        foods.setEntityStatus(EntityStatus.ACTIVE);
        return foodMapper.toFoodResponse(foodRepository.save(foods));
    }

    public List<FoodResponse> createFoods(List<FoodRequest> requests) {
        return requests.stream().map(this::createFood).toList();
    }

    public void changeStatus(long id, EntityStatus entityStatus) {
        Foods food = foodRepository.findByFoodId(id)
                .orElseThrow(() -> new AppException(ErrorCode.FOOD_NOT_FOUND));
        food.setEntityStatus(entityStatus);
        foodRepository.save(food);
    }
}
