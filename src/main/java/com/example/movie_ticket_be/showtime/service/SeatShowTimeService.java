package com.example.movie_ticket_be.showtime.service;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.showtime.dto.response.SeatSelectionResponse;
import com.example.movie_ticket_be.showtime.dto.response.SeatShowTimeResponse;
import com.example.movie_ticket_be.showtime.dto.response.SeatSummaryResponse;
import com.example.movie_ticket_be.showtime.dto.response.SuggestedSeatResponse;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import com.example.movie_ticket_be.showtime.mapper.SeatShowTimeMapper;
import com.example.movie_ticket_be.showtime.repository.SeatShowTimeRepository;
import com.example.movie_ticket_be.showtime.repository.ShowTimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatShowTimeService {
    SeatShowTimeMapper seatShowTimeMapper;
    SeatShowTimeRepository seatShowTimeRepository;
    ShowTimeRepository showTimeRepository;
    ShowTimePriceService showTimePriceService;

    public List<SeatShowTimeResponse> getAllSeatShowTimesByShowTime(Long showTimeId) {
        if (!showTimeRepository.existsByShowTimeId(showTimeId)) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
        }
        return seatShowTimeRepository.findByShowTimes_ShowTimeId(showTimeId).stream()
                .map(seatShowTimeMapper::toSeatShowTimeResponse)
                .toList();
    }

    public SeatSelectionResponse getSeatSelectionData(Long showTimeId) {
        List<SeatShowTimeResponse> seats = getAllSeatShowTimesByShowTime(showTimeId);
        Map<SeatType, BigDecimal> pricingMap = showTimePriceService.getPriceMapByShowTime(showTimeId);

        List<SuggestedSeatResponse> suggested = seats.stream()
                .filter(s -> s.getSeatShowTimeStatus() == SeatShowTimeStatus.AVAILABLE)
                .sorted(Comparator.comparing(SeatShowTimeResponse::getViewQuanlityScore,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(3)
                .map(s -> SuggestedSeatResponse.builder()
                        .seatShowTimeId(s.getSeatShowTimeId())
                        .seatRow(s.getSeatRow())
                        .seatNumber(s.getSeatNumber())
                        .seatType(s.getSeatType())
                        .viewQuanlityScore(s.getViewQuanlityScore())
                        .build())
                .toList();

        return SeatSelectionResponse.builder()
                .seats(seats)
                .pricingMap(pricingMap)
                .suggested(suggested)
                .build();
    }

    public void generateSeatsForShowTime(Long showTimeId, Long roomId) {
        seatShowTimeRepository.bulkInsertSeatsForShowTime(showTimeId, roomId);
    }

    public boolean hasBookedSeats(Long showTimeId) {
        return seatShowTimeRepository.existsByShowTimes_ShowTimeIdAndSeatShowTimeStatusIn(
                showTimeId, List.of(SeatShowTimeStatus.RESERVED, SeatShowTimeStatus.SOLD));
    }

    public SeatSummaryResponse getSeatSummaryForShowTime(Long showTimeId) {
        long available = seatShowTimeRepository.countByShowTimes_ShowTimeIdAndSeatShowTimeStatus(showTimeId, SeatShowTimeStatus.AVAILABLE);
        long sold = seatShowTimeRepository.countByShowTimes_ShowTimeIdAndSeatShowTimeStatus(showTimeId, SeatShowTimeStatus.SOLD);
        long reserved = seatShowTimeRepository.countByShowTimes_ShowTimeIdAndSeatShowTimeStatus(showTimeId, SeatShowTimeStatus.RESERVED);
        long blocked = seatShowTimeRepository.countByShowTimes_ShowTimeIdAndSeatShowTimeStatus(showTimeId, SeatShowTimeStatus.BLOCKED);
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
