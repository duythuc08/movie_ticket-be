package com.example.movie_ticket_be.showtime.mapper;

import com.example.movie_ticket_be.showtime.dto.request.ShowTimePriceRequest;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimePriceResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimePrice;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShowTimePriceMapper {
	ShowTimePrice toShowTimePrice(ShowTimePriceRequest request);

	@Mapping(source = "showTimePriceId", target = "seatShowTimePriceId")
	@Mapping(source = "showtimes.showTimeId", target = "showTimeId")
	ShowTimePriceResponse toShowTimePriceResponse(ShowTimePrice showTimePrice);
}
