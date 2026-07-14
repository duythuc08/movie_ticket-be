package com.example.movie_ticket_be.booking.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderFoodsRequest {
	@NotNull
	Long foodId;
	@NotNull
	@Min(value = 1, message = "Số lượng phải ít nhất là 1")
	Integer quantity;
}
