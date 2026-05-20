package com.example.movie_ticket_be.user.dto.response;

import com.example.movie_ticket_be.core.enums.EntityStatus;
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
    Long tierId;
    String name;
    String description;
    Long pointsRequired;
    BigDecimal discountPercent;
    BigDecimal birthdayDiscount;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    EntityStatus entityStatus;
}
