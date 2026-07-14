package com.example.movie_ticket_be.user.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipTierRequest {
	@NotBlank
	String name;
	String description;
	@Min(value = 0, message = "Điểm yêu cầu không được âm")
	Long pointsRequired;
	@DecimalMin(value = "0", message = "Phần trăm giảm giá không được âm")
	@DecimalMax(value = "100", message = "Phần trăm giảm giá không được vượt 100%")
	BigDecimal discountPercent;
	@DecimalMin(value = "0", message = "Giảm giá sinh nhật không được âm")
	@DecimalMax(value = "100", message = "Giảm giá sinh nhật không được vượt 100%")
	BigDecimal birthdayDiscount;
}
