package com.example.movie_ticket_be.promotion.dto.response;

import com.example.movie_ticket_be.promotion.enums.PromotionType;
import com.example.movie_ticket_be.showtime.enums.DayOfWeek;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserVoucherResponse {
    Long voucherId;
    Long promotionId;
    String code;
    String name;
    String description;
    PromotionType type;
    BigDecimal discountValue;
    BigDecimal minOrderValue;
    BigDecimal maxDiscountAmount;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Set<DayOfWeek> dayOfWeek;
    Set<Long> applicableMovieIds;
    LocalDateTime claimedAt;
    Boolean eligible;
}
