package com.example.movie_ticket_be.booking.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderFoodResponse {
    Long foodId;
    String name;
    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal totalPrice;
}
