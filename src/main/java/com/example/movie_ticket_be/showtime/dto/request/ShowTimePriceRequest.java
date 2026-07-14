package com.example.movie_ticket_be.showtime.dto.request;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTimePriceRequest {
	Long showTimeId;
	@NotNull
	SeatType seatType;
	@NotNull
	@DecimalMin(value = "0.01", message = "Giá phải lớn hơn 0")
	BigDecimal price;
}
