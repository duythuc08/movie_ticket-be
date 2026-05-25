package com.example.movie_ticket_be.cinema.mapper;

import com.example.movie_ticket_be.cinema.dto.response.SeatResponse;
import com.example.movie_ticket_be.cinema.entity.Seats;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(source = "rooms.roomId", target = "roomId")
    SeatResponse toSeatResponse(Seats seats);
}
