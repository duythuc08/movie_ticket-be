package com.example.movie_ticket_be.cinema.dto.request;

import com.example.movie_ticket_be.cinema.enums.FoodStatus;
import com.example.movie_ticket_be.cinema.enums.FoodType;
import jakarta.persistence.Lob;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FoodRequest {
    Long cinemaId;
    String name;
    @Lob
    String description;
    BigDecimal price;
    String imageUrl;
    Boolean isCombo;
    Integer stockQuantity;
    FoodType foodType;
    FoodStatus foodStatus;
}
