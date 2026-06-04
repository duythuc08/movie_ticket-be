package com.example.movie_ticket_be.showtime.dto.request;

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
	List<LocalDateTime> startTimes;
	Long movieId;
	Long roomId;
	List<ShowTimePriceRequest> prices;
}
