package com.example.movie_ticket_be.cinema.service;

import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.cinema.mapper.CinemaMapper;
import com.example.movie_ticket_be.cinema.repository.CinemaRepository;
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
public class CinemaService {
    CinemaRepository cinemaRepository;
    CinemaMapper cinemaMapper;

    public List<CinemaResponse> getCinemas() {
        return cinemaRepository.findAll().stream()
                .map(cinemaMapper::toCinemasResponse)
                .toList();
    }

    public CinemaResponse getCinemaById(Long cinemaId) {
        return cinemaMapper.toCinemasResponse(
                cinemaRepository.findByCinemaId(cinemaId)
                        .orElseThrow(() -> new AppException(ErrorCode.CINEMA_NOT_FOUND)));
    }
}
