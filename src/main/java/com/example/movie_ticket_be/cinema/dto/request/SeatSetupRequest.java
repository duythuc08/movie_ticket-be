package com.example.movie_ticket_be.cinema.dto.request;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SeatSetupRequest {
	@Min(value = 1, message = "Số hàng phải ít nhất 1")
	@Max(value = 30, message = "Số hàng tối đa 30")
	int rows;

	@Min(value = 1, message = "Số cột phải ít nhất 1")
	@Max(value = 30, message = "Số cột tối đa 30")
	int cols;

	@NotNull(message = "Bố cục ghế không được để trống")
	SeatType[][] seatTypes;
}
