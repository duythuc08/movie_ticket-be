package com.example.movie_ticket_be.user.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipTierResponse {
    String name;
    String description;
    Long pointsRequired;
    BigDecimal discountPercent;
    BigDecimal birthdayDiscount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
