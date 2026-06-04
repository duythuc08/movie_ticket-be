package com.example.movie_ticket_be.showtime.dto.response;

import com.example.movie_ticket_be.cinema.enums.RoomType;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.showtime.enums.ShowTimeStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTimeResponse {
	Long showTimeId;
	LocalDateTime startTime;
	LocalDateTime endTime;
	ShowTimeStatus showTimeStatus;

	Long movieId;
	String movieTitle;
	Integer movieDuration;
	String moviePosterUrl;
	MovieStatus movieStatus;

	Long roomId;
	String roomName;
	RoomType roomType;

	Long cinemaId;
	String cinemaName;

	List<ShowTimePriceResponse> prices;
}
