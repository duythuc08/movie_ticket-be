package com.example.movie_ticket_be.showtime.mapper;

import com.example.movie_ticket_be.showtime.dto.request.SeatShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.response.SeatShowTimeResponse;
import com.example.movie_ticket_be.showtime.entity.SeatShowTime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatShowTimeMapper {

    SeatShowTime toSeatShowTime(SeatShowTimeRequest request);

    @Mapping(source = "seatShowTimeId", target = "seatShowTimeId")
    @Mapping(source = "users.userId", target = "userId")

    @Mapping(source = "seats.seatId", target = "seatId")
    @Mapping(source = "seats.seatRow", target = "seatRow")
    @Mapping(source = "seats.seatNumber", target = "seatNumber")
    @Mapping(source = "seats.seatType", target = "seatType")

    @Mapping(source = "showTimes.showTimeId", target = "showTimeId")
    @Mapping(source = "showTimes.rooms.roomId", target = "roomId")
    @Mapping(source = "showTimes.rooms.name", target = "roomName")
    @Mapping(source = "showTimes.rooms.roomType", target = "roomType")
    SeatShowTimeResponse toSeatShowTimeResponse(SeatShowTime seatShowTime);
}