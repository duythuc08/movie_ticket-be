package com.example.movie_ticket_be.cinema.dto.response;

import com.example.movie_ticket_be.cinema.enums.FoodStatus;
import com.example.movie_ticket_be.cinema.enums.FoodType;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FoodResponse {
    Long foodId;
    Long cinemaId;
    String cinemaName;
    String name;
    String description;
    BigDecimal price;
    String imageUrl;
    Boolean isCombo;
    Integer stockQuantity;
    FoodType foodType;
    FoodStatus foodStatus;
    EntityStatus entityStatus;
}
