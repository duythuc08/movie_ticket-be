package com.example.movie_ticket_be.cinema.mapper;

import com.example.movie_ticket_be.cinema.dto.request.FoodRequest;
import com.example.movie_ticket_be.cinema.dto.response.FoodResponse;
import com.example.movie_ticket_be.cinema.entity.Foods;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface FoodMapper {

    @Mapping(target = "cinema", ignore = true)
    @Mapping(target = "foodId", ignore = true)
    @Mapping(target = "entityStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Foods toFoods(FoodRequest request);

    @Mapping(source = "cinema.cinemaId", target = "cinemaId")
    @Mapping(source = "cinema.name", target = "cinemaName")
    FoodResponse toFoodResponse(Foods foods);

    @Mapping(target = "cinema", ignore = true)
    @Mapping(target = "foodId", ignore = true)
    @Mapping(target = "entityStatus", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFoods(FoodRequest request, @MappingTarget Foods foods);
}
