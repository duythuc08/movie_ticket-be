package com.example.movie_ticket_be.promotion.dto.request;

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
public class PromotionRequest {
    String code;
    String name;
    String description;
    PromotionType type;
    BigDecimal discountValue;
    BigDecimal minOrderValue;
    BigDecimal maxDiscountAmount;
    Integer useLimit;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Set<Long> applicableMovieIds;
    Set<DayOfWeek> dayOfWeek;
}
