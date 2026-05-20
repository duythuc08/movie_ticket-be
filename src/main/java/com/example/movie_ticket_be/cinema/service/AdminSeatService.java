package com.example.movie_ticket_be.cinema.service;

import com.example.movie_ticket_be.cinema.dto.response.SeatResponse;
import com.example.movie_ticket_be.cinema.entity.Seats;
import com.example.movie_ticket_be.cinema.mapper.SeatMapper;
import com.example.movie_ticket_be.cinema.repository.SeatRepository;
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
public class AdminSeatService {
    SeatRepository seatRepository;
    SeatMapper seatMapper;

    public List<SeatResponse> getSeatsByRoom(Long roomId) {
        return seatRepository.findByRooms_RoomId(roomId).stream()
                .map(seatMapper::toSeatResponse)
                .toList();
    }

    public void changeStatus(long id, EntityStatus entityStatus) {
        Seats seat = seatRepository.findBySeatId(id)
                .orElseThrow(() -> new AppException(ErrorCode.SEAT_NOT_FOUND));
        seat.setEntityStatus(entityStatus);
        seatRepository.save(seat);
    }
}
