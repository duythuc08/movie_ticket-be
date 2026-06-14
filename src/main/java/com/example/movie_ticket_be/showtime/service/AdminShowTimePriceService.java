package com.example.movie_ticket_be.showtime.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.cinema.enums.SeatType;
import com.example.movie_ticket_be.cinema.service.AdminSeatService;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimePriceRequest;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimePriceResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimePrice;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import com.example.movie_ticket_be.showtime.mapper.ShowTimePriceMapper;
import com.example.movie_ticket_be.showtime.repository.ShowTimePriceRepository;
import com.example.movie_ticket_be.showtime.repository.ShowTimeRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminShowTimePriceService {
	ShowTimePriceRepository showTimePriceRepository;
	ShowTimePriceMapper showTimePriceMapper;
	ShowTimeRepository showTimeRepository;
	AdminSeatService adminSeatService;
	SeatShowTimeService seatShowTimeService;

	public ShowTimePriceResponse createShowTimePrice(ShowTimePriceRequest request) {
		ShowTimes showTime = showTimeRepository.findById(request.getShowTimeId())
				.orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

		Set<SeatType> validTypes = adminSeatService.getDistinctSeatTypesByRoomId(showTime.getRooms().getRoomId());
		if (!validTypes.contains(request.getSeatType())) {
			throw new AppException(ErrorCode.SEAT_TYPE_NOT_FOUND);
		}

		if (showTimePriceRepository.existsByShowtimes_ShowTimeIdAndSeatType(request.getShowTimeId(),
				request.getSeatType())) {
			throw new AppException(ErrorCode.SHOWTIME_PRICE_EXISTED);
		}

		ShowTimePrice showTimePrice = showTimePriceMapper.toShowTimePrice(request);
		showTimePrice.setShowtimes(showTime);
		return showTimePriceMapper.toShowTimePriceResponse(showTimePriceRepository.save(showTimePrice));
	}

	public List<ShowTimePriceResponse> createShowTimePrices(List<ShowTimePriceRequest> requests) {
		return requests.stream().map(this::createShowTimePrice).toList();
	}

	public List<ShowTimePriceResponse> getPricesByShowTimeId(Long showTimeId) {
		return showTimePriceRepository.findByShowtimes_ShowTimeId(showTimeId).stream()
				.map(showTimePriceMapper::toShowTimePriceResponse).toList();
	}

	public ShowTimePriceResponse updateShowTimePrice(Long showTimePriceId, Long showTimeId, BigDecimal price) {
		ShowTimePrice showTimePrice = showTimePriceRepository.findById(showTimePriceId)
				.orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_PRICE_NOT_FOUND));

		if (!showTimePrice.getShowtimes().getShowTimeId().equals(showTimeId)) {
			throw new AppException(ErrorCode.SHOWTIME_NOT_FOUND);
		}

		if (seatShowTimeService.hasBookedSeats(showTimeId)) {
			throw new AppException(ErrorCode.SHOWTIME_HAS_ACTIVE_SEATS);
		}

		showTimePrice.setPrice(price);
		return showTimePriceMapper.toShowTimePriceResponse(showTimePriceRepository.save(showTimePrice));
	}
}
