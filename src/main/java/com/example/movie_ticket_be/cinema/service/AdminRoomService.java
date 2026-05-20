package com.example.movie_ticket_be.cinema.service;

import com.example.movie_ticket_be.cinema.dto.request.RoomRequest;
import com.example.movie_ticket_be.cinema.dto.response.RoomResponse;
import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.mapper.RoomMapper;
import com.example.movie_ticket_be.cinema.repository.RoomRepository;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
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
public class AdminRoomService {
    RoomRepository roomRepository;
    RoomMapper roomMapper;

    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByNameAndCinemas_CinemaId(request.getName(), request.getCinemas().getCinemaId())) {
            throw new AppException(ErrorCode.ROOM_EXISTED);
        }
        Rooms rooms = roomMapper.toRooms(request);
        rooms.setEntityStatus(EntityStatus.ACTIVE);
        return roomMapper.toRoomResponse(roomRepository.save(rooms));
    }

    public List<RoomResponse> createRooms(List<RoomRequest> requests) {
        return requests.stream().map(this::createRoom).toList();
    }

    public void changeStatus(long id, EntityStatus entityStatus) {
        Rooms room = roomRepository.findByRoomId(id)
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        room.setEntityStatus(entityStatus);
        roomRepository.save(room);
    }
}
