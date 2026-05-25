package com.example.movie_ticket_be.cinema.mapper;

import com.example.movie_ticket_be.cinema.dto.request.AdminCinemaUpdateRequest;
import com.example.movie_ticket_be.cinema.dto.request.CinemaRequest;
import com.example.movie_ticket_be.cinema.dto.response.AdminCinemaResponse;
import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.cinema.entity.Rooms;
import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CinemaMapper {
    Cinemas toCinemas(CinemaRequest request);

    CinemaResponse toCinemasResponse(Cinemas cinemas);

    @Mapping(source = "rooms", target = "rooms")
    AdminCinemaResponse toAdminCinemaResponse(Cinemas cinemas, List<Rooms> rooms);

    AdminCinemaResponse.AdminRoomResponse toAdminRoomResponse(Rooms room);

    @Mapping(target = "cinemaId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "entityStatus", ignore = true)
    void updateCinema(AdminCinemaUpdateRequest request, @MappingTarget Cinemas cinema);
}
