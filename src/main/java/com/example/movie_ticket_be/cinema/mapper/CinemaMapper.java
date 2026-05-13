package com.example.movie_ticket_be.cinema.mapper;

import com.example.movie_ticket_be.cinema.dto.request.CinemaRequest;
import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.cinema.entity.Cinemas;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CinemaMapper {
    Cinemas toCinemas(CinemaRequest request);

    CinemaResponse toCinemasResponse(Cinemas cinemas);
}
