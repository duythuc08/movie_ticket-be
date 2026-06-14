package com.example.movie_ticket_be.showtime.service;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimePriceResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimePrice;
import com.example.movie_ticket_be.showtime.mapper.ShowTimePriceMapper;
import com.example.movie_ticket_be.showtime.repository.ShowTimePriceRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShowTimePriceService {
	ShowTimePriceRepository showTimePriceRepository;
	ShowTimePriceMapper showTimePriceMapper;

	public List<ShowTimePriceResponse> getAllPriceByShowTime(Long showTimeId) {
		return showTimePriceRepository.findByShowtimes_ShowTimeId(showTimeId).stream()
				.map(showTimePriceMapper::toShowTimePriceResponse).toList();
	}

	public ShowTimePriceResponse getPriceByShowTimeAndSeatType(Long showTimeId, SeatType seatType) {
		ShowTimePrice showTimePrice = showTimePriceRepository
				.findByShowtimes_ShowTimeIdAndSeatType(showTimeId, seatType)
				.orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_PRICE_NOT_FOUND));
		return showTimePriceMapper.toShowTimePriceResponse(showTimePrice);
	}

	public Map<SeatType, BigDecimal> getPriceMapByShowTime(Long showTimeId) {
		List<ShowTimePrice> priceList = showTimePriceRepository.findByShowtimes_ShowTimeId(showTimeId);
		if (priceList.isEmpty()) {
			return new java.util.HashMap<>();
		}
		return priceList.stream().collect(Collectors.toMap(ShowTimePrice::getSeatType, ShowTimePrice::getPrice));
	}
}
