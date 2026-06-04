package com.example.movie_ticket_be.showtime.mapper;

import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {ShowTimePriceMapper.class})
public interface ShowTimeMapper {

	@Mapping(source = "movies.movieId", target = "movieId")
	@Mapping(source = "movies.title", target = "movieTitle")
	@Mapping(source = "movies.duration", target = "movieDuration")
	@Mapping(source = "movies.posterUrl", target = "moviePosterUrl")
	@Mapping(source = "movies.movieStatus", target = "movieStatus")
	@Mapping(source = "rooms.roomId", target = "roomId")
	@Mapping(source = "rooms.name", target = "roomName")
	@Mapping(source = "rooms.roomType", target = "roomType")
	@Mapping(source = "rooms.cinemas.cinemaId", target = "cinemaId")
	@Mapping(source = "rooms.cinemas.name", target = "cinemaName")
	@Mapping(source = "prices", target = "prices")
	ShowTimeResponse toShowTimeResponse(ShowTimes showTimes);
}
