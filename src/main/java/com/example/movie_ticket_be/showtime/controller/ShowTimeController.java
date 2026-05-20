package com.example.movie_ticket_be.showtime.controller;

import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.showtime.dto.response.QuickBookingSlotResponse;
import com.example.movie_ticket_be.showtime.dto.response.ShowTimeResponse;
import com.example.movie_ticket_be.showtime.service.ShowTimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/showtimes")
@RequiredArgsConstructor
public class ShowTimeController {
    private final ShowTimeService showTimeService;

    @GetMapping("/getShowTimes")
    public ApiResponse<List<ShowTimeResponse>> getShowTimeAll() {
        return ApiResponse.<List<ShowTimeResponse>>builder()
                .result(showTimeService.getAllShowTimes())
                .build();
    }

    @GetMapping("/getShowTimes/by-movie-time/{movieId}")
    public ApiResponse<List<ShowTimeResponse>> getShowTimesByMovieAndTimeRange(
            @PathVariable Long movieId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ApiResponse.<List<ShowTimeResponse>>builder()
                .result(showTimeService.getShowTimesByMovieAndTimeRange(movieId, start, end))
                .build();
    }

    @GetMapping("/getShowTimes/by-cinema/{cinemaId}")
    public ApiResponse<List<ShowTimeResponse>> getShowTimesByCinemaAndMovie(
            @PathVariable Long cinemaId,
            @RequestParam Long movieId) {
        return ApiResponse.<List<ShowTimeResponse>>builder()
                .result(showTimeService.getShowTimesByCinemaAndMovie(cinemaId, movieId, LocalDateTime.now()))
                .build();
    }

    @GetMapping("/getShowTimes/active/by-movie/{movieId}")
    public ApiResponse<List<ShowTimeResponse>> getActiveShowTimesByMovieAndRange(
            @PathVariable Long movieId,
            @RequestParam LocalDateTime start,
            @RequestParam LocalDateTime end) {
        return ApiResponse.<List<ShowTimeResponse>>builder()
                .result(showTimeService.getActiveShowTimesByMovieAndRange(movieId, start, end))
                .build();
    }

    @GetMapping("/getShowTimes/by-movie/{movieId}")
    public ApiResponse<List<ShowTimeResponse>> getShowTimesByMovie(@PathVariable Long movieId) {
        return ApiResponse.<List<ShowTimeResponse>>builder()
                .result(showTimeService.getShowTimesByMovie(movieId))
                .build();
    }

    @GetMapping("/movies-by-cinema/{cinemaId}")
    public ApiResponse<List<MovieResponse>> getMoviesByCinema(@PathVariable Long cinemaId) {
        return ApiResponse.<List<MovieResponse>>builder()
                .result(showTimeService.getMoviesByCinema(cinemaId))
                .build();
    }

    @GetMapping("/dates")
    public ApiResponse<List<String>> getAvailableDates(@RequestParam Long cinemaId, @RequestParam Long movieId) {
        return ApiResponse.<List<String>>builder()
                .result(showTimeService.getAvailableDatesByCinemaAndMovie(cinemaId, movieId))
                .build();
    }

    @GetMapping("/slots")
    public ApiResponse<List<QuickBookingSlotResponse>> getShowtimeSlots(
            @RequestParam Long cinemaId,
            @RequestParam Long movieId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ApiResponse.<List<QuickBookingSlotResponse>>builder()
                .result(showTimeService.getShowtimeSlotsByCinemaMovieDate(cinemaId, movieId, date))
                .build();
    }

    @GetMapping("/now-showing-movies")
    public ApiResponse<List<MovieResponse>> getNowShowingMoviesForQuickBooking() {
        return ApiResponse.<List<MovieResponse>>builder()
                .result(showTimeService.getNowShowingMoviesForQuickBooking())
                .build();
    }

    @GetMapping("/cinemas-by-movie/{movieId}")
    public ApiResponse<List<CinemaResponse>> getCinemasByMovie(@PathVariable Long movieId) {
        return ApiResponse.<List<CinemaResponse>>builder()
                .result(showTimeService.getCinemasByMovie(movieId))
                .build();
    }
}
