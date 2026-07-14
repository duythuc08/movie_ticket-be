package com.example.movie_ticket_be.cinema.dto.request;

import com.example.movie_ticket_be.cinema.enums.SeatStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminSeatStatusUpdateRequest {
	@NotNull
	SeatStatus seatStatus;
	List<Long> unlockShowTimeIds;
}
