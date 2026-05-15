package com.example.movie_ticket_be.cinema.mapper;


import com.example.movie_ticket_be.cinema.dto.request.RoomRequest;
import com.example.movie_ticket_be.cinema.dto.response.RoomResponse;
import com.example.movie_ticket_be.cinema.entity.Rooms;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {
    Rooms toRooms(RoomRequest request);

    RoomResponse toRoomResponse(Rooms rooms);
}
