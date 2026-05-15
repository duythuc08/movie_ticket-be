package com.example.movie_ticket_be.showtime.mapper;

import com.example.movie_ticket_be.showtime.dto.request.ShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ShowTimeMapper {
    ShowTimes toShowTimes(ShowTimeRequest request);

    ShowTimeResponse toShowTimeResponse(ShowTimes showTimes);
}
