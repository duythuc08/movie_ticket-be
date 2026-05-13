package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.request.MovieCreationRequest;
import com.example.movie_ticket_be.movie.dto.response.AdminMovieResponse;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.movie.dto.response.PagedMovieResponse;
import com.example.movie_ticket_be.movie.service.MovieService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;



@Slf4j
@RestController
@RequestMapping("/movies")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class MovieController {
    MovieService movieService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    ApiResponse<MovieResponse> createMovie(@RequestPart("request") @Valid MovieCreationRequest request,
                                           @RequestPart(value = "poster", required = false) MultipartFile posterFile){
        return ApiResponse.<MovieResponse>builder()
                .result(movieService.createMovie(request, posterFile))
                .message("Thêm phim mới thành công")
                .build();
    }

    @GetMapping
    ApiResponse<List<AdminMovieResponse>> getAdminMovie(){
        return ApiResponse.<List<AdminMovieResponse>>builder()
                .result(movieService.getAdminMovies())
                .build();
    }

    @GetMapping("/getMovies")
    ApiResponse<List<MovieResponse>> getMovies(){
        return ApiResponse.<List<MovieResponse>>builder()
                .result(movieService.getMovies())
                .build();
    }
    @GetMapping("/{id}")
    public ApiResponse<MovieResponse> getMovieById(@PathVariable Long id) {
        return ApiResponse.<MovieResponse>builder()
                .result(movieService.getMovieById(id))
                .build();
    }

    @GetMapping("/showing")
    ApiResponse<List<MovieResponse>> getShowingMovies(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        List<MovieResponse> result = (page != null && size != null)
                ? movieService.getMoviesShowingPaged(page, size)
                : movieService.getMoviesShowing();
        return ApiResponse.<List<MovieResponse>>builder().result(result).build();
    }

    @GetMapping("/comingSoon")
    ApiResponse<List<MovieResponse>> getComingSoonMovies(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        List<MovieResponse> result = (page != null && size != null)
                ? movieService.getMoviesComingSoonPaged(page, size)
                : movieService.getMoviesComingSoon();
        return ApiResponse.<List<MovieResponse>>builder().result(result).build();
    }

    @GetMapping("/imax")
    ApiResponse<List<MovieResponse>> getImaxMovies(
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size) {
        List<MovieResponse> result = (page != null && size != null)
                ? movieService.getMoviesImaxPaged(page, size)
                : movieService.getMoviesImax();
        return ApiResponse.<List<MovieResponse>>builder().result(result).build();
    }

    @GetMapping("/showing/paged")
    ApiResponse<PagedMovieResponse> getShowingMoviesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {
        return ApiResponse.<PagedMovieResponse>builder()
                .result(movieService.getShowingMoviesPagedResponse(page, size))
                .build();
    }

    @GetMapping("/comingSoon/paged")
    ApiResponse<PagedMovieResponse> getComingSoonMoviesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {
        return ApiResponse.<PagedMovieResponse>builder()
                .result(movieService.getComingSoonMoviesPagedResponse(page, size))
                .build();
    }

    @GetMapping("/imax/paged")
    ApiResponse<PagedMovieResponse> getImaxMoviesPaged(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "4") int size) {
        return ApiResponse.<PagedMovieResponse>builder()
                .result(movieService.getImaxMoviesPagedResponse(page, size))
                .build();
    }
}
