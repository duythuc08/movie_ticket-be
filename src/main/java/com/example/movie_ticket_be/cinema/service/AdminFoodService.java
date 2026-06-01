package com.example.movie_ticket_be.cinema.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.cinema.dto.request.FoodRequest;
import com.example.movie_ticket_be.cinema.dto.response.FoodResponse;
import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.cinema.entity.Foods;
import com.example.movie_ticket_be.cinema.mapper.FoodMapper;
import com.example.movie_ticket_be.cinema.repository.CinemaRepository;
import com.example.movie_ticket_be.cinema.repository.FoodRepository;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminFoodService {
    FoodRepository foodRepository;
    FoodMapper foodMapper;
    CinemaRepository cinemaRepository;

    public FoodResponse createFood(Long cinemaId, FoodRequest request) {
        Cinemas cinema = cinemaRepository.findByCinemaId(cinemaId)
                .orElseThrow(() -> new AppException(ErrorCode.CINEMA_NOT_FOUND));
        if (foodRepository.existsByNameAndCinema_CinemaId(request.getName(), cinemaId)) {
            throw new AppException(ErrorCode.FOOD_EXISTED);
        }
        Foods food = foodMapper.toFoods(request);
        food.setCinema(cinema);
        food.setEntityStatus(EntityStatus.ACTIVE);
        return foodMapper.toFoodResponse(foodRepository.save(food));
    }

    public List<FoodResponse> createFoods(Long cinemaId, List<FoodRequest> requests) {
        return requests.stream().map(req -> createFood(cinemaId, req)).toList();
    }

    public Page<FoodResponse> getFoods(Long cinemaId, Specification<Foods> spec, Pageable pageable) {
        Specification<Foods> cinemaSpec = (root, query, cb) ->
                cb.equal(root.get("cinema").get("cinemaId"), cinemaId);
        return foodRepository.findAll(Specification.where(cinemaSpec).and(spec), pageable)
                .map(foodMapper::toFoodResponse);
    }

    public FoodResponse updateFood(long id, FoodRequest request) {
        Foods food = foodRepository.findByFoodId(id)
                .orElseThrow(() -> new AppException(ErrorCode.FOOD_NOT_FOUND));
        Long cinemaId = food.getCinema().getCinemaId();
        if (!food.getName().equals(request.getName())
                && foodRepository.existsByNameAndCinema_CinemaIdAndFoodIdNot(request.getName(), cinemaId, id)) {
            throw new AppException(ErrorCode.FOOD_EXISTED);
        }
        foodMapper.updateFoods(request, food);
        return foodMapper.toFoodResponse(foodRepository.save(food));
    }

    public void changeStatus(long id, EntityStatus entityStatus) {
        Foods food = foodRepository.findByFoodId(id)
                .orElseThrow(() -> new AppException(ErrorCode.FOOD_NOT_FOUND));
        food.setEntityStatus(entityStatus);
        foodRepository.save(food);
    }
}
