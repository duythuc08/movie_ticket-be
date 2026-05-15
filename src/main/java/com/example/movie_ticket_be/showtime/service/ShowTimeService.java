package com.example.movie_ticket_be.showtime.service;


import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.mapper.CinemaMapper;
import com.example.movie_ticket_be.cinema.repository.RoomRepository;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.movie.mapper.MovieMapper;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.request.UpdateShowTimeRequest;
import com.example.movie_ticket_be.showtime.enums.SeatShowTimeStatus;
import com.example.movie_ticket_be.showtime.dto.response.QuickBookingSlotResponse;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;
import com.example.movie_ticket_be.showtime.entity.ShowTimes;
import com.example.movie_ticket_be.showtime.enums.ShowTimeStatus;
import com.example.movie_ticket_be.showtime.mapper.ShowTimeMapper;
import com.example.movie_ticket_be.showtime.repository.SeatShowTimeRepository;
import com.example.movie_ticket_be.showtime.repository.ShowTimeRepository;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShowTimeService {
    ShowTimeRepository showTimeRepository;
    ShowTimeMapper showTimeMapper;
    MovieMapper movieMapper;
    CinemaMapper cinemaMapper;

    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private final SeatShowTimeRepository seatShowTimeRepository;

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ShowTimeResponse createShowTime(ShowTimeRequest request) {

        Rooms room = roomRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));


        Movies movie = movieRepository.findByMovieId(request.getMovieId())
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));

        if (showTimeRepository.existsConflictingShowtime(
                request.getRoomId(),
                request.getStartTime(),
                request.getEndTime())) {
            throw new AppException(ErrorCode.SHOWTIME_EXISTED);
        }

        ShowTimes showTimes = showTimeMapper.toShowTimes(request);

        showTimes.setRooms(room);
        showTimes.setMovies(movie);

        ShowTimes savedShowTime = showTimeRepository.save(showTimes);

        seatShowTimeRepository.bulkInsertSeatsForShowTime(
                savedShowTime.getShowTimeId(),
                request.getRoomId()
        );

        return showTimeMapper.toShowTimeResponse(savedShowTime);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public ShowTimeResponse updateShowTime(Long showTimeId, UpdateShowTimeRequest request) {
        ShowTimes showTime = showTimeRepository.findByShowTimeId(showTimeId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));

        if (showTime.getShowTimeStatus() != ShowTimeStatus.SCHEDULED) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_UPDATABLE);
        }

        boolean hasActiveSeats = seatShowTimeRepository.existsByShowTimes_ShowTimeIdAndSeatShowTimeStatusIn(
                showTimeId,
                List.of(SeatShowTimeStatus.RESERVED, SeatShowTimeStatus.SOLD, SeatShowTimeStatus.BLOCKED)
        );
        if (hasActiveSeats) {
            throw new AppException(ErrorCode.SHOWTIME_HAS_ACTIVE_SEATS);
        }

        if (request.getStartTime() != null) showTime.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) showTime.setEndTime(request.getEndTime());
        if (request.getMovieId() != null) {
            Movies movie = movieRepository.findByMovieId(request.getMovieId())
                    .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
            showTime.setMovies(movie);
        }
        if (request.getRoomId() != null) {
            Rooms room = roomRepository.findByRoomId(request.getRoomId())
                    .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
            showTime.setRooms(room);
        }

        return showTimeMapper.toShowTimeResponse(showTimeRepository.save(showTime));
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
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

    @Transactional
    public void autoUpdateShowTimeStatus() {
        LocalDateTime now = LocalDateTime.now();

        showTimeRepository.findByShowTimeStatusAndStartTimeLessThanEqual(ShowTimeStatus.SCHEDULED, now)
                .forEach(st -> st.setShowTimeStatus(ShowTimeStatus.ONGOING));

        showTimeRepository.findByShowTimeStatusAndEndTimeLessThanEqual(ShowTimeStatus.ONGOING, now)
                .forEach(st -> st.setShowTimeStatus(ShowTimeStatus.COMPLETED));

        showTimeRepository.findFullyBookedCandidates()
                .forEach(st -> st.setShowTimeStatus(ShowTimeStatus.FULLY_BOOKED));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<ShowTimeResponse> createShowTimes(List<ShowTimeRequest> requests) {
        return requests.stream()
                .map(this::createShowTime)
                .toList();
    }

    public List<ShowTimeResponse> getAllShowTimes() {
        return showTimeRepository.findAllWithDetails()
                .stream()
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }

    public List<ShowTimeResponse> getShowTimesByMovieAndTimeRange(Long movieId, LocalDateTime start, LocalDateTime end) {
        return showTimeRepository.findByMovies_MovieIdAndStartTimeBetween(movieId, start, end)
                .stream()
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }

    public List<ShowTimeResponse> getShowTimesByCinemaAndMovie(Long cinemaId, Long movieId, LocalDateTime now) {
        return showTimeRepository.findByRooms_Cinemas_CinemaIdAndMovies_MovieIdAndStartTimeAfter(cinemaId, movieId, now)
                .stream()
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }

    public List<ShowTimeResponse> getActiveShowTimesByMovieAndRange(Long movieId, LocalDateTime start, LocalDateTime end) {
        return showTimeRepository.findByMovies_MovieIdAndStartTimeBetweenAndShowTimeStatusNot(movieId, start, end, ShowTimeStatus.CANCELLED)
                .stream()
                .sorted(Comparator.comparing(ShowTimes::getStartTime))
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }

    public List<ShowTimeResponse> getShowTimesByMovie(Long movieId){
        return  showTimeRepository.findByMovies_MovieId(movieId)
                .stream()
                .map(showTimeMapper::toShowTimeResponse)
                .toList();
    }

    public List<MovieResponse> getMoviesByCinema(Long cinemaId) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        List<MovieStatus> activeStatuses = List.of(MovieStatus.NOW_SHOWING, MovieStatus.IMAX);
        return showTimeRepository.findDistinctMoviesByCinemaId(cinemaId, startOfToday, activeStatuses)
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }

    public List<String> getAvailableDatesByCinemaAndMovie(Long cinemaId, Long movieId) {
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        return showTimeRepository.findByCinemaIdAndMovieIdAfterNow(cinemaId, movieId, startOfToday)
                .stream()
                .map(st -> st.getStartTime().toLocalDate().toString())
                .distinct()
                .sorted()
                .toList();
    }

    public List<QuickBookingSlotResponse> getShowtimeSlotsByCinemaMovieDate(Long cinemaId, Long movieId, LocalDate date) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = date.equals(LocalDate.now()) ? now : date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        return showTimeRepository.findByCinemaIdAndMovieIdOnDate(cinemaId, movieId, startOfDay, endOfDay)
                .stream()
                .map(st -> QuickBookingSlotResponse.builder()
                        .showTimeId(st.getShowTimeId())
                        .startTime(st.getStartTime().format(timeFormatter))
                        .roomName(st.getRooms() != null ? st.getRooms().getName() : "")
                        .roomType(st.getRooms() != null ? st.getRooms().getRoomType().name() : "")
                        .build())
                .toList();
    }

    public List<MovieResponse> getNowShowingMoviesForQuickBooking() {
        LocalDateTime now = LocalDateTime.now();
        List<MovieStatus> activeStatuses = List.of(MovieStatus.NOW_SHOWING, MovieStatus.IMAX);
        return showTimeRepository.findNowShowingMoviesWithUpcomingSlots(now, activeStatuses)
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }

    public List<CinemaResponse> getCinemasByMovie(Long movieId) {
        LocalDateTime now = LocalDateTime.now();
        return showTimeRepository.findDistinctCinemasByMovieId(movieId, now)
                .stream()
                .map(cinemaMapper::toCinemasResponse)
                .toList();
    }
}