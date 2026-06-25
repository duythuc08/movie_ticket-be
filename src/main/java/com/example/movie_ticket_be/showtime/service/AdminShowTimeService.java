package com.example.movie_ticket_be.showtime.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.repository.CinemaRepository;
import com.example.movie_ticket_be.cinema.repository.RoomRepository;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimePriceRequest;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.request.UpdateShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.response.SeatSummaryResponse;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeDetailResponse;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimePriceResponse;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import com.example.movie_ticket_be.showtime.enums.ShowTimeStatus;
import com.example.movie_ticket_be.showtime.mapper.ShowTimeMapper;
import com.example.movie_ticket_be.showtime.repository.SeatShowTimeRepository;
import com.example.movie_ticket_be.showtime.repository.ShowTimeRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminShowTimeService {
	static final int BUFFER_MINUTES = 15;
	static final int CLEANING_MINUTES = 15;

	ShowTimeRepository showTimeRepository;
	ShowTimeMapper showTimeMapper;
	MovieRepository movieRepository;
	RoomRepository roomRepository;
	CinemaRepository cinemaRepository;
	SeatShowTimeRepository seatShowTimeRepository;
	SeatShowTimeService seatShowTimeService;
	AdminShowTimePriceService adminShowTimePriceService;

	@Transactional
	public List<ShowTimeResponse> createShowTimes(ShowTimeRequest request) {
		Rooms room = roomRepository.findByRoomId(request.getRoomId())
				.orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
		Movies movie = movieRepository.findByMovieId(request.getMovieId())
				.orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

		if (movie.getMovieStatus() == MovieStatus.STOPPED) {
			throw new AppException(ErrorCode.MOVIE_STOPPED);
		}

		int totalMinutes = movie.getDuration() + BUFFER_MINUTES;

		List<LocalDateTime> sortedStarts = request.getStartTimes().stream().sorted().distinct().toList();
		
		LocalDateTime now = LocalDateTime.now();
		for (LocalDateTime start : sortedStarts) {
			if (start.isBefore(now)) {
				throw new AppException(ErrorCode.SHOWTIME_IN_PAST);
			}
		}

		for (int i = 0; i < sortedStarts.size() - 1; i++) {
			LocalDateTime endA = sortedStarts.get(i).plusMinutes(totalMinutes);
			LocalDateTime startB = sortedStarts.get(i + 1);
			if (startB.isBefore(endA)) {
				throw new AppException(ErrorCode.SHOWTIME_TIME_OVERLAP);
			}
			if (startB.isBefore(endA.plusMinutes(CLEANING_MINUTES))) {
				throw new AppException(ErrorCode.SHOWTIME_BUFFER_CONFLICT);
			}
		}

		for (LocalDateTime start : sortedStarts) {
			LocalDateTime end = start.plusMinutes(totalMinutes);
			checkRoomTimeConflict(request.getRoomId(), start, end, null);
		}

		List<ShowTimes> toSave = sortedStarts.stream().map(start -> {
			ShowTimes st = ShowTimes.builder().startTime(start)
					.endTime(roundUpToNearest5(start.plusMinutes(totalMinutes))).rooms(room).movies(movie)
					.showTimeStatus(ShowTimeStatus.SCHEDULED).build();
			return st;
		}).toList();

		List<ShowTimes> saved = showTimeRepository.saveAll(toSave);

		saved.forEach(st -> {
			if (request.getPrices() != null && !request.getPrices().isEmpty()) {
				List<ShowTimePriceRequest> priceRequests = request.getPrices().stream().map(p -> ShowTimePriceRequest
						.builder().showTimeId(st.getShowTimeId()).seatType(p.getSeatType()).price(p.getPrice()).build())
						.toList();
				adminShowTimePriceService.createShowTimePrices(priceRequests);
			}
			seatShowTimeService.generateSeatsForShowTime(st.getShowTimeId(), request.getRoomId());
		});

		return saved.stream().map(st -> {
			ShowTimeResponse response = showTimeMapper.toShowTimeResponse(st);
			response.setPrices(adminShowTimePriceService.getPricesByShowTimeId(st.getShowTimeId()));
			return response;
		}).toList();
	}

	@Transactional
	public ShowTimeResponse updateShowTime(Long showTimeId, UpdateShowTimeRequest request) {
		ShowTimes showTime = showTimeRepository.findByShowTimeId(showTimeId)
				.orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));
		if (showTime.getShowTimeStatus() != ShowTimeStatus.SCHEDULED) {
			throw new AppException(ErrorCode.SHOWTIME_NOT_UPDATABLE);
		}
		boolean hasActiveSeats = seatShowTimeRepository.existsByShowTimes_ShowTimeIdAndSeatShowTimeStatusIn(showTimeId,
				List.of(SeatShowTimeStatus.RESERVED, SeatShowTimeStatus.SOLD, SeatShowTimeStatus.BLOCKED));
		if (hasActiveSeats)
			throw new AppException(ErrorCode.SHOWTIME_HAS_ACTIVE_SEATS);

		if (request.getMovieId() != null) {
			Movies movie = movieRepository.findByMovieId(request.getMovieId())
					.orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
			if (movie.getMovieStatus() == MovieStatus.STOPPED) {
				throw new AppException(ErrorCode.MOVIE_STOPPED);
			}
			showTime.setMovies(movie);
		}
		if (request.getRoomId() != null) {
			Rooms room = roomRepository.findByRoomId(request.getRoomId())
					.orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
			showTime.setRooms(room);
		}
		if (request.getStartTime() != null) {
			int totalMinutes = showTime.getMovies().getDuration() + BUFFER_MINUTES;
			showTime.setStartTime(request.getStartTime());
			showTime.setEndTime(roundUpToNearest5(request.getStartTime().plusMinutes(totalMinutes)));
		}

		checkRoomTimeConflict(showTime.getRooms().getRoomId(), showTime.getStartTime(), showTime.getEndTime(),
				showTimeId);

		return showTimeMapper.toShowTimeResponse(showTimeRepository.save(showTime));
	}

	@Transactional
	public ShowTimeResponse cancelShowTime(Long showTimeId) {
		ShowTimes showTime = showTimeRepository.findByShowTimeId(showTimeId)
				.orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));
		if (showTime.getShowTimeStatus() == ShowTimeStatus.CANCELLED) {
			throw new AppException(ErrorCode.SHOWTIME_ALREADY_CANCELLED);
		}
		seatShowTimeRepository.deleteNonSoldSeatsByShowTime(showTimeId);
		showTime.setShowTimeStatus(ShowTimeStatus.CANCELLED);
		return showTimeMapper.toShowTimeResponse(showTimeRepository.save(showTime));
	}

	public Page<ShowTimeResponse> getAdminShowTimes(Specification<ShowTimes> spec, Pageable pageable) {
		return showTimeRepository.findAll(spec, pageable).map(showTimeMapper::toShowTimeResponse);
	}

	public List<ShowTimeResponse> getShowTimeForGanttChart(Long cinemaId, LocalDate day) {
		if (!cinemaRepository.existsById(cinemaId)) {
			throw new AppException(ErrorCode.CINEMA_NOT_FOUND);
		}
		LocalDateTime dayStart = day.atStartOfDay();
		LocalDateTime dayEnd = day.atTime(LocalTime.MAX);
		return showTimeRepository.findByRooms_Cinemas_CinemaIdAndStartTimeBetween(cinemaId, dayStart, dayEnd).stream()
				.map(showTimeMapper::toShowTimeResponse).toList();
	}

	public ShowTimeDetailResponse getAdminShowTimeDetail(Long showTimeId) {
		ShowTimes showTime = showTimeRepository.findByShowTimeId(showTimeId)
				.orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

		List<ShowTimePriceResponse> prices = adminShowTimePriceService.getPricesByShowTimeId(showTimeId);
		SeatSummaryResponse seatSummary = seatShowTimeService.getSeatSummaryForShowTime(showTimeId);

		return ShowTimeDetailResponse.builder().showTimeId(showTime.getShowTimeId()).startTime(showTime.getStartTime())
				.endTime(showTime.getEndTime())
				.showTimeStatus(showTime.getShowTimeStatus() != null ? showTime.getShowTimeStatus().name() : null)
				.movies(showTime.getMovies()).rooms(showTime.getRooms()).prices(prices).seatSummary(seatSummary)
				.build();
	}

	private static LocalDateTime roundUpToNearest5(LocalDateTime dt) {
		int remainder = dt.getMinute() % 5;
		if (remainder == 0)
			return dt.withSecond(0).withNano(0);
		return dt.plusMinutes(5 - remainder).withSecond(0).withNano(0);
	}

	private void checkRoomTimeConflict(Long roomId, LocalDateTime start, LocalDateTime end, Long excludeId) {
		boolean overlap = excludeId == null
				? showTimeRepository.existsConflictingShowtime(roomId, start, end)
				: showTimeRepository.existsConflictingShowtimeExcluding(roomId, start, end, excludeId);
		if (overlap) {
			throw new AppException(ErrorCode.SHOWTIME_TIME_OVERLAP);
		}

		boolean gapViolation = excludeId == null
				? showTimeRepository.existsConflictingShowtime(roomId, start.minusMinutes(CLEANING_MINUTES),
						end.plusMinutes(CLEANING_MINUTES))
				: showTimeRepository.existsConflictingShowtimeExcluding(roomId, start.minusMinutes(CLEANING_MINUTES),
						end.plusMinutes(CLEANING_MINUTES), excludeId);
		if (gapViolation) {
			throw new AppException(ErrorCode.SHOWTIME_BUFFER_CONFLICT);
		}
	}
}
