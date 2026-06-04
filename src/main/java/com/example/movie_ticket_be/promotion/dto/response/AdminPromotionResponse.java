package com.example.movie_ticket_be.promotion.dto.response;

import com.example.movie_ticket_be.promotion.enums.PromotionStatus;
import com.example.movie_ticket_be.promotion.enums.PromotionType;
import com.example.movie_ticket_be.showtime.enums.DayOfWeek;
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
public class AdminPromotionResponse {
	Long promotionId;
	String code;
	String name;
	String description;
	PromotionType type;
	BigDecimal discountValue;
	BigDecimal minOrderValue;
	BigDecimal maxDiscountAmount;
	Integer useLimit;
	Integer usedCount;
	LocalDateTime startTime;
	LocalDateTime endTime;
	PromotionStatus status;
	Set<Long> applicableMovieIds;
	Set<DayOfWeek> dayOfWeek;
	LocalDateTime createdAt;
	LocalDateTime updatedAt;
}
