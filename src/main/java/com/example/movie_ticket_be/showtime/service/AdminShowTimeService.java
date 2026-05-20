package com.example.movie_ticket_be.showtime.service;

import com.example.movie_ticket_be.cinema.entity.Rooms;
import com.example.movie_ticket_be.cinema.repository.RoomRepository;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.showtime.dto.request.ShowTimeRequest;
import com.example.movie_ticket_be.showtime.dto.request.UpdateShowTimeRequest;
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
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminShowTimeService {
    ShowTimeRepository showTimeRepository;
    ShowTimeMapper showTimeMapper;
    MovieRepository movieRepository;
    RoomRepository roomRepository;
    SeatShowTimeRepository seatShowTimeRepository;

    @Transactional
    public ShowTimeResponse createShowTime(ShowTimeRequest request) {
        Rooms room = roomRepository.findByRoomId(request.getRoomId())
                .orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
        Movies movie = movieRepository.findByMovieId(request.getMovieId())
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        if (showTimeRepository.existsConflictingShowtime(request.getRoomId(), request.getStartTime(), request.getEndTime())) {
            throw new AppException(ErrorCode.SHOWTIME_EXISTED);
        }
        ShowTimes showTimes = showTimeMapper.toShowTimes(request);
        showTimes.setRooms(room);
        showTimes.setMovies(movie);
        ShowTimes saved = showTimeRepository.save(showTimes);
        seatShowTimeRepository.bulkInsertSeatsForShowTime(saved.getShowTimeId(), request.getRoomId());
        return showTimeMapper.toShowTimeResponse(saved);
    }

    public List<ShowTimeResponse> createShowTimes(List<ShowTimeRequest> requests) {
        return requests.stream().map(this::createShowTime).toList();
    }

    @Transactional
    public ShowTimeResponse updateShowTime(Long showTimeId, UpdateShowTimeRequest request) {
        ShowTimes showTime = showTimeRepository.findByShowTimeId(showTimeId)
                .orElseThrow(() -> new AppException(ErrorCode.SHOWTIME_NOT_FOUND));
        if (showTime.getShowTimeStatus() != ShowTimeStatus.SCHEDULED) {
            throw new AppException(ErrorCode.SHOWTIME_NOT_UPDATABLE);
        }
        boolean hasActiveSeats = seatShowTimeRepository.existsByShowTimes_ShowTimeIdAndSeatShowTimeStatusIn(
                showTimeId, List.of(SeatShowTimeStatus.RESERVED, SeatShowTimeStatus.SOLD, SeatShowTimeStatus.BLOCKED));
        if (hasActiveSeats) throw new AppException(ErrorCode.SHOWTIME_HAS_ACTIVE_SEATS);
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
}
