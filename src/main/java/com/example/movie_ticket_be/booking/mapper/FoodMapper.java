package com.example.movie_ticket_be.booking.mapper;

import com.example.movie_ticket_be.booking.dto.request.FoodRequest;
import com.example.movie_ticket_be.booking.dto.response.FoodResponse;
import com.example.movie_ticket_be.booking.entity.Foods;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FoodMapper {
    Foods toFoods(FoodRequest request);
    FoodResponse toFoodResponse(Foods foods);
}
