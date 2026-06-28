package com.example.movie_ticket_be.showtime.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.cinema.mapper.CinemaMapper;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.movie.mapper.MovieMapper;
import com.example.movie_ticket_be.showtime.dto.response.QuickBookingSlotResponse;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import com.example.movie_ticket_be.showtime.enums.ShowTimeStatus;
import com.example.movie_ticket_be.showtime.mapper.ShowTimeMapper;
import com.example.movie_ticket_be.showtime.repository.ShowTimeRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShowTimeService {
	ShowTimeRepository showTimeRepository;
	ShowTimeMapper showTimeMapper;
	MovieMapper movieMapper;
	CinemaMapper cinemaMapper;

	@Transactional
	public void autoUpdateShowTimeStatus() {
		LocalDateTime now = LocalDateTime.now();

		// SCHEDULED -> ONGOING
		List<ShowTimes> toOngoing = showTimeRepository.findByShowTimeStatusAndStartTimeLessThanEqual(ShowTimeStatus.SCHEDULED, now);
		toOngoing.forEach(st -> st.setShowTimeStatus(ShowTimeStatus.ONGOING));
		if (!toOngoing.isEmpty()) {
		    log.info("autoUpdateShowTimeStatus: Updated {} showtime(s) to ONGOING: {}",
			    toOngoing.size(),
			    toOngoing.stream()
				    .map(st -> st.getShowTimeId() + "(" + (st.getMovies() != null ? st.getMovies().getTitle() : "-") + "," + st.getStartTime() + ")")
				    .collect(Collectors.joining(", "))
		    );
		}

		// ONGOING -> COMPLETED
		List<ShowTimes> toCompleted = showTimeRepository.findByShowTimeStatusAndEndTimeLessThanEqual(ShowTimeStatus.ONGOING, now);
		toCompleted.forEach(st -> st.setShowTimeStatus(ShowTimeStatus.COMPLETED));
		if (!toCompleted.isEmpty()) {
		    log.info("autoUpdateShowTimeStatus: Updated {} showtime(s) to COMPLETED: {}",
			    toCompleted.size(),
			    toCompleted.stream()
				    .map(st -> st.getShowTimeId() + "(" + (st.getMovies() != null ? st.getMovies().getTitle() : "-") + "," + st.getEndTime() + ")")
				    .collect(Collectors.joining(", "))
		    );
		}

		// Mark fully booked candidates
		List<ShowTimes> toFullyBooked = showTimeRepository.findFullyBookedCandidates();
		toFullyBooked.forEach(st -> st.setShowTimeStatus(ShowTimeStatus.FULLY_BOOKED));
		if (!toFullyBooked.isEmpty()) {
		    log.info("autoUpdateShowTimeStatus: Updated {} showtime(s) to FULLY_BOOKED: {}",
			    toFullyBooked.size(),
			    toFullyBooked.stream()
				    .map(st -> st.getShowTimeId() + "(" + (st.getMovies() != null ? st.getMovies().getTitle() : "-") + "," + st.getStartTime() + ")")
				    .collect(Collectors.joining(", "))
		    );
		}
	}

	@Transactional(readOnly = true)
	public List<ShowTimeResponse> getAllShowTimes() {
		return showTimeRepository.findAllWithDetails().stream().map(showTimeMapper::toShowTimeResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<ShowTimeResponse> getShowTimesByMovieAndTimeRange(Long movieId, LocalDateTime start,
			LocalDateTime end) {
		return showTimeRepository.findByMovies_MovieIdAndStartTimeBetween(movieId, start, end).stream()
				.map(showTimeMapper::toShowTimeResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<ShowTimeResponse> getShowTimesByCinemaAndMovie(Long cinemaId, Long movieId, LocalDateTime now) {
		return showTimeRepository.findByRooms_Cinemas_CinemaIdAndMovies_MovieIdAndStartTimeAfter(cinemaId, movieId, now)
				.stream().map(showTimeMapper::toShowTimeResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<ShowTimeResponse> getActiveShowTimesByMovieAndRange(Long movieId, LocalDateTime start,
			LocalDateTime end) {
		return showTimeRepository
				.findByMovies_MovieIdAndStartTimeBetweenAndShowTimeStatusNot(movieId, start, end,
						ShowTimeStatus.CANCELLED)
				.stream().sorted(Comparator.comparing(ShowTimes::getStartTime)).map(showTimeMapper::toShowTimeResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<ShowTimeResponse> getShowTimesByMovie(Long movieId) {
		return showTimeRepository.findByMovies_MovieId(movieId).stream().map(showTimeMapper::toShowTimeResponse)
				.toList();
	}

	@Transactional(readOnly = true)
	public List<MovieResponse> getMoviesByCinema(Long cinemaId) {
		LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
		return showTimeRepository.findDistinctMoviesByCinemaId(cinemaId, startOfToday, List.of(MovieStatus.NOW_SHOWING))
				.stream().map(movieMapper::toMovieResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<String> getAvailableDatesByCinemaAndMovie(Long cinemaId, Long movieId) {
		LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
		return showTimeRepository.findByCinemaIdAndMovieIdAfterNow(cinemaId, movieId, startOfToday).stream()
				.map(st -> st.getStartTime().toLocalDate().toString()).distinct().sorted().toList();
	}

	@Transactional(readOnly = true)
	public List<QuickBookingSlotResponse> getShowtimeSlotsByCinemaMovieDate(Long cinemaId, Long movieId,
			LocalDate date) {
		LocalDateTime now = LocalDateTime.now();
		LocalDateTime startOfDay = date.equals(LocalDate.now()) ? now : date.atStartOfDay();
		LocalDateTime endOfDay = date.atTime(23, 59, 59);
		DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
		return showTimeRepository.findByCinemaIdAndMovieIdOnDate(cinemaId, movieId, startOfDay, endOfDay).stream()
				.map(st -> QuickBookingSlotResponse.builder().showTimeId(st.getShowTimeId())
						.startTime(st.getStartTime().format(timeFormatter))
						.roomName(st.getRooms() != null ? st.getRooms().getName() : "")
						.roomType(st.getRooms() != null ? st.getRooms().getRoomType().name() : "").build())
				.toList();
	}

	@Transactional(readOnly = true)
	public List<MovieResponse> getNowShowingMoviesForQuickBooking() {
		LocalDateTime now = LocalDateTime.now();
		return showTimeRepository.findNowShowingMoviesWithUpcomingSlots(now, List.of(MovieStatus.NOW_SHOWING)).stream()
				.map(movieMapper::toMovieResponse).toList();
	}

	@Transactional(readOnly = true)
	public List<CinemaResponse> getCinemasByMovie(Long movieId) {
		LocalDateTime now = LocalDateTime.now();
		return showTimeRepository.findDistinctCinemasByMovieId(movieId, now).stream()
				.map(cinemaMapper::toCinemasResponse).toList();
	}
}
