package com.example.movie_ticket_be.showtime.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SuggestedSeatResponse {
	Long seatShowTimeId;
	String seatRow;
	Integer seatNumber;
	String seatType;
	BigDecimal viewQuanlityScore;
}
