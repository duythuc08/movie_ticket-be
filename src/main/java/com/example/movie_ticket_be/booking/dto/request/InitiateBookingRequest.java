package com.example.movie_ticket_be.booking.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InitiateBookingRequest {
	@NotBlank
	String userId;
	@NotEmpty
	List<Long> seatShowTimeIds;
}
