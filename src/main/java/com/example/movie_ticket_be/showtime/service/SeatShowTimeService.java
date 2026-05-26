package com.example.movie_ticket_be.showtime.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.showtime.dto.response.SeatShowTimeResponse;
import com.example.movie_ticket_be.showtime.dto.response.SeatSummaryResponse;
import com.example.movie_ticket_be.showtime.mapper.SeatShowTimeMapper;
import com.example.movie_ticket_be.showtime.repository.SeatShowTimeRepository;
import com.example.movie_ticket_be.showtime.repository.ShowTimeRepository;
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
public class SeatShowTimeService {
    SeatShowTimeMapper seatShowTimeMapper;
    SeatShowTimeRepository seatShowTimeRepository;
    ShowTimeRepository showTimeRepository;

    public List<SeatShowTimeResponse> getAllSeatShowTimesByShowTime(Long showTimeId) {
        if (!showTimeRepository.existsByShowTimeId(showTimeId)) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
        }
        return seatShowTimeRepository.findByShowTimes_ShowTimeId(showTimeId).stream()
                .map(seatShowTimeMapper::toSeatShowTimeResponse)
                .toList();
    }

    public void generateSeatsForShowTime(Long showTimeId, Long roomId) {
        seatShowTimeRepository.bulkInsertSeatsForShowTime(showTimeId, roomId);
    }

    public SeatSummaryResponse getSeatSummaryForShowTime(Long showTimeId) {
        long available = seatShowTimeRepository.countByShowTimes_ShowTimeIdAndSeatShowTimeStatus(showTimeId, com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus.AVAILABLE);
        long sold = seatShowTimeRepository.countByShowTimes_ShowTimeIdAndSeatShowTimeStatus(showTimeId, com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus.SOLD);
        long reserved = seatShowTimeRepository.countByShowTimes_ShowTimeIdAndSeatShowTimeStatus(showTimeId, com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus.RESERVED);
        long blocked = seatShowTimeRepository.countByShowTimes_ShowTimeIdAndSeatShowTimeStatus(showTimeId, com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus.BLOCKED);
        long total = available + sold + reserved + blocked;
        return SeatSummaryResponse.builder()
                .total(total)
                .available(available)
                .sold(sold)
                .reserved(reserved)
                .blocked(blocked)
                .build();
    }
}
