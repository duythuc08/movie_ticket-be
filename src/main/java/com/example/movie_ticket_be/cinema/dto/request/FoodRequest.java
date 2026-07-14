package com.example.movie_ticket_be.cinema.dto.request;

import com.example.movie_ticket_be.cinema.enums.FoodStatus;
import com.example.movie_ticket_be.cinema.enums.FoodType;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FoodRequest {
	@NotBlank
	String name;
	@Lob
	String description;
	@DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
	BigDecimal price;
	String imageUrl;
	Boolean isCombo;
	@Min(value = 0, message = "Số lượng không được âm")
	Integer stockQuantity;
	FoodType foodType;
	FoodStatus foodStatus;
}
