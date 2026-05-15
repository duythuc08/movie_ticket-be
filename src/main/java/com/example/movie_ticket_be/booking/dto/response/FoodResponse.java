package com.example.movie_ticket_be.booking.dto.response;

import com.example.movie_ticket_be.booking.enums.FoodStatus;
import com.example.movie_ticket_be.booking.enums.FoodType;
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
    String name;
    String description;
    BigDecimal price;
    String imageUrl;
    Boolean isCombo;
    Integer stockQuantity;
    FoodType foodType;
    FoodStatus foodStatus;
}
