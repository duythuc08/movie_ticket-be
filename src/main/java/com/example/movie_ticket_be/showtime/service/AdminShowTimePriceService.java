package com.example.movie_ticket_be.showtime.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimePriceRequest;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimePriceResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimePrice;
import com.example.movie_ticket_be.showtime.mapper.ShowTimePriceMapper;
import com.example.movie_ticket_be.showtime.repository.ShowTimePriceRepository;
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
public class AdminShowTimePriceService {
    ShowTimePriceRepository showTimePriceRepository;
    ShowTimePriceMapper showTimePriceMapper;

    public ShowTimePriceResponse createShowTimePrice(ShowTimePriceRequest request) {
        if (showTimePriceRepository.existsByShowtimes_ShowTimeIdAndSeatType(request.getShowTimeId(), request.getSeatType())) {
            throw new AppException(ErrorCode.SHOWTIME_PRICE_EXISTED);
        }
        ShowTimePrice showTimePrice = showTimePriceMapper.toShowTimePrice(request);
        return showTimePriceMapper.toShowTimePriceResponse(showTimePriceRepository.save(showTimePrice));
    }

    public List<ShowTimePriceResponse> createShowTimePrices(List<ShowTimePriceRequest> requests) {
        return requests.stream().map(this::createShowTimePrice).toList();
    }
}
