package com.example.movie_ticket_be.cinema.service;

import com.example.movie_ticket_be.cinema.enums.FoodStatus;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.cinema.dto.response.FoodResponse;
import com.example.movie_ticket_be.cinema.mapper.FoodMapper;
import com.example.movie_ticket_be.cinema.repository.FoodRepository;
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
public class FoodService {
	FoodRepository foodRepository;
	FoodMapper foodMapper;

	public List<FoodResponse> getAllFoodsByCinema(Long cinemaId) {
		return foodRepository
				.findByCinema_CinemaIdAndEntityStatusAndFoodStatus(cinemaId, EntityStatus.ACTIVE, FoodStatus.IN_STOCK)
				.stream().map(foodMapper::toFoodResponse).toList();
	}

	public FoodResponse getFoodById(Long foodId) {
		return foodMapper.toFoodResponse(
				foodRepository.findByFoodId(foodId).orElseThrow(() -> new AppException(ErrorCode.FOOD_NOT_FOUND)));
	}
}
