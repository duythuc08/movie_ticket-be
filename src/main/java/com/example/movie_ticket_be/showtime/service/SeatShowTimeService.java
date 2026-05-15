package com.example.movie_ticket_be.showtime.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.showtime.dto.response.SeatShowTimeResponse;
import com.example.movie_ticket_be.showtime.mapper.SeatShowTimeMapper;
import com.example.movie_ticket_be.showtime.repository.SeatShowTimeRepository;
import com.example.movie_ticket_be.showtime.repository.ShowTimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatShowTimeService {
    SeatShowTimeMapper seatShowTimeMapper;
    SeatShowTimeRepository seatShowTimeRepository;

    final ShowTimeRepository showTimeRepository;

    @PreAuthorize("isAuthenticated()")
    public List<SeatShowTimeResponse> getAllSeatShowTimesByShowTime(Long showTimeId) {
        if(!showTimeRepository.existsByShowTimeId(showTimeId)){
            throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
        }
        return seatShowTimeRepository.findByShowTimes_ShowTimeId(showTimeId)
                .stream()
                .map(seatShowTimeMapper::toSeatShowTimeResponse)
                .toList();
    }

}
