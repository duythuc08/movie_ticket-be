package com.example.movie_ticket_be.showtime.mapper;

import com.example.movie_ticket_be.showtime.dto.request.ShowTimePriceRequest;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimePriceResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimePrice;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShowTimePriceMapper {
    ShowTimePrice toShowTimePrice(ShowTimePriceRequest request);
    ShowTimePriceResponse toShowTimePriceResponse(ShowTimePrice showTimePrice);
}
