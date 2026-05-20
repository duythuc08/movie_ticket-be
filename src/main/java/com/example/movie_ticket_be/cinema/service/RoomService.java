package com.example.movie_ticket_be.cinema.service;

import com.example.movie_ticket_be.cinema.dto.response.RoomResponse;
import com.example.movie_ticket_be.cinema.enums.RoomStatus;
import com.example.movie_ticket_be.cinema.enums.RoomType;
import com.example.movie_ticket_be.cinema.mapper.RoomMapper;
import com.example.movie_ticket_be.cinema.repository.RoomRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomService {
    RoomRepository roomRepository;
    RoomMapper roomMapper;

    public List<RoomResponse> getRooms() {
        return roomRepository.findAll().stream()
                .map(roomMapper::toRoomResponse)
                .toList();
    }

    public List<RoomResponse> getRoomsByCinemaId(Long cinemaId) {
        return roomRepository.findByCinemas_CinemaId(cinemaId).stream()
                .map(roomMapper::toRoomResponse)
                .toList();
    }

    public List<RoomResponse> getRoomsByCinemaIdAndStatus(Long cinemaId, RoomStatus status) {
        return roomRepository.findByCinemas_CinemaIdAndRoomStatus(cinemaId, status).stream()
                .map(roomMapper::toRoomResponse)
                .toList();
    }

    public List<RoomResponse> getRoomsByCinemaIdAndType(Long cinemaId, RoomType type) {
        return roomRepository.findByCinemas_CinemaIdAndRoomType(cinemaId, type).stream()
                .map(roomMapper::toRoomResponse)
                .toList();
    }
}
