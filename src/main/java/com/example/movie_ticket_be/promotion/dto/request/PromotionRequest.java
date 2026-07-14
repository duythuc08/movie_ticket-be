package com.example.movie_ticket_be.promotion.dto.request;

import com.example.movie_ticket_be.promotion.enums.PromotionType;
import com.example.movie_ticket_be.showtime.enums.DayOfWeek;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PromotionRequest {
	@NotBlank
	String code;
	@NotBlank
	String name;
	String description;
	@NotNull
	PromotionType type;
	@NotNull
	@DecimalMin(value = "0.01", message = "Giá trị giảm phải lớn hơn 0")
	BigDecimal discountValue;
	@DecimalMin(value = "0", message = "Giá trị đơn hàng tối thiểu không được âm")
	BigDecimal minOrderValue;
	@DecimalMin(value = "0", message = "Giảm tối đa không được âm")
	BigDecimal maxDiscountAmount;
	Integer useLimit;
	Boolean isPublic;
	@NotNull
	LocalDateTime startTime;
	@NotNull
	LocalDateTime endTime;
	Set<Long> applicableMovieIds;
	Set<DayOfWeek> dayOfWeek;
}
