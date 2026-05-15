package com.example.movie_ticket_be.showtime.service;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimePriceRequest;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimePriceResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimePrice;
import com.example.movie_ticket_be.showtime.mapper.ShowTimePriceMapper;
import com.example.movie_ticket_be.showtime.repository.ShowTimePriceRepository;
import com.example.movie_ticket_be.showtime.repository.ShowTimeRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class ShowTimePriceService {
    ShowTimeRepository showTimeRepository;
    ShowTimePriceMapper showTimePriceMapper;
    private final ShowTimePriceRepository showTimePriceRepository;

    @PreAuthorize("hasRole('ADMIN')")
    public ShowTimePriceResponse createShowTimePrice(ShowTimePriceRequest request){
        if (showTimePriceRepository.existsByShowtimes_ShowTimeIdAndSeatType(request.getShowTimeId(),request.getSeatType())){
            throw new AppException(ErrorCode.SHOWTIME_PRICE_EXISTED);
        }
        ShowTimePrice showTimePrice = showTimePriceMapper.toShowTimePrice(request);
        return showTimePriceMapper.toShowTimePriceResponse(showTimePriceRepository.save(showTimePrice));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ShowTimePriceResponse> createShowTimePrices(List<ShowTimePriceRequest> requests){
        return requests.stream()
                .map(this::createShowTimePrice)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    public List<ShowTimePriceResponse> getAllPriceByShowTime(Long showTimeId){
        return showTimePriceRepository.findByShowtimes_ShowTimeId(showTimeId)
                .stream()
                .map(showTimePriceMapper::toShowTimePriceResponse)
                .toList();
    }

    @PreAuthorize("isAuthenticated()")
    public ShowTimePriceResponse getPriceByShowTimeAndSeatType(Long showTimeId, SeatType seatType){
        ShowTimePrice showTimePrice = showTimePriceRepository.findByShowtimes_ShowTimeIdAndSeatType(showTimeId,seatType)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_PRICE_NOT_FOUND));
        return showTimePriceMapper.toShowTimePriceResponse(showTimePrice);
    }
    @PreAuthorize("isAuthenticated()")
    public Map<SeatType, BigDecimal> getPriceMapByShowTime(Long showTimeId) {
        List<ShowTimePrice> priceList = showTimePriceRepository.findByShowtimes_ShowTimeId(showTimeId);

        if (priceList.isEmpty()) {
            throw new RuntimeException("Lỗi: Suất chiếu " + showTimeId + " chưa được cấu hình giá!");
        }

        return priceList.stream()
                .collect(Collectors.toMap(
                        ShowTimePrice::getSeatType,
                        ShowTimePrice::getPrice
                ));
    }
}
