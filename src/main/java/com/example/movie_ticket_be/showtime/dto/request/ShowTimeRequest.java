package com.example.movie_ticket_be.showtime.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTimeRequest {
	@NotEmpty
	List<LocalDateTime> startTimes;
	@NotNull
	Long movieId;
	@NotNull
	Long roomId;
	@NotEmpty
	List<ShowTimePriceRequest> prices;
}
