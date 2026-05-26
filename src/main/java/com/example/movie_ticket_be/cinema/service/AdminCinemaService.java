package com.example.movie_ticket_be.cinema.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.example.movie_ticket_be.cinema.dto.request.AdminCinemaUpdateRequest;
import com.example.movie_ticket_be.cinema.dto.request.CinemaRequest;
import com.example.movie_ticket_be.cinema.dto.response.AdminCinemaResponse;
import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import com.example.movie_ticket_be.cinema.entity.Cinemas;
import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.mapper.CinemaMapper;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.cinema.repository.CinemaRepository;
import com.example.movie_ticket_be.cinema.repository.RoomRepository;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCinemaService {
    CinemaRepository cinemaRepository;
    RoomRepository roomRepository;
    CinemaMapper cinemaMapper;
    AdminRoomService adminRoomService;

    public CinemaResponse createCinema(CinemaRequest request) {
        if (cinemaRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.CINEMA_EXISTED);
        }
        Cinemas cinemas = cinemaMapper.toCinemas(request);
        return cinemaMapper.toCinemasResponse(cinemaRepository.save(cinemas));
    }

    public List<CinemaResponse> createCinemas(List<CinemaRequest> requests) {
        return requests.stream().map(this::createCinema).toList();
    }

    public Page<CinemaResponse> getCinemas(Specification<Cinemas> spec, Pageable pageable) {
        return cinemaRepository.findAll(spec, pageable).map(cinemaMapper::toCinemasResponse);
    }

    public AdminCinemaResponse getAdminCinemaById(long id) {
        Cinemas cinemas = cinemaRepository.findByCinemaId(id)
                .orElseThrow(() -> new AppException(ErrorCode.CINEMA_NOT_FOUND));
        List<Rooms> rooms = roomRepository.findByCinemas_CinemaId(id);
        AdminCinemaResponse response = cinemaMapper.toAdminCinemaResponse(cinemas, rooms);
        response.setRooms(rooms.stream().map(cinemaMapper::toAdminRoomResponse).toList());
        return response;
    }

    public List<CinemaResponse> getCinemasByStatus(CinemaStatus status) {
        return cinemaRepository.findByCinemaStatus(status).stream()
                .map(cinemaMapper::toCinemasResponse)
                .toList();
    }

    @Transactional
    public AdminCinemaResponse updateCinema(long cinemaId, AdminCinemaUpdateRequest request) {
        Cinemas cinema = cinemaRepository.findByCinemaId(cinemaId)
                .orElseThrow(() -> new AppException(ErrorCode.CINEMA_NOT_FOUND));

        if (!cinema.getName().equals(request.getName()) &&
                cinemaRepository.existsByNameAndCinemaIdNot(request.getName(), cinemaId)) {
            throw new AppException(ErrorCode.CINEMA_EXISTED);
        }

        cinemaMapper.updateCinema(request, cinema);
        cinemaRepository.save(cinema);

        if (!CollectionUtils.isEmpty(request.getRooms())) {
            adminRoomService.updateRoomsForCinema(cinema, request.getRooms());
        }

        List<Rooms> rooms = roomRepository.findByCinemas_CinemaId(cinemaId);
        AdminCinemaResponse response = cinemaMapper.toAdminCinemaResponse(cinema, rooms);
        response.setRooms(rooms.stream().map(cinemaMapper::toAdminRoomResponse).toList());
        return response;
    }

    public void changeStatus(long id, EntityStatus status) {
        Cinemas cinema = cinemaRepository.findByCinemaId(id)
                .orElseThrow(() -> new AppException(ErrorCode.CINEMA_NOT_FOUND));
        cinema.setEntityStatus(status);
        cinemaRepository.save(cinema);
    }
}
