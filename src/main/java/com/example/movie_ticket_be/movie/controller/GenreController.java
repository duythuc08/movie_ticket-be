package com.example.movie_ticket_be.movie.controller;


import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.request.GenreCreationRequest;
import com.example.movie_ticket_be.movie.dto.response.GenreResponse;
import com.example.movie_ticket_be.movie.service.GenreService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genre")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class GenreController {
    GenreService genreService;

    @PostMapping
    ApiResponse<GenreResponse> createGenre(@RequestBody @Valid GenreCreationRequest request) {
        return ApiResponse.<GenreResponse>builder()
                .result(genreService.createGenre(request))
                .message("Thêm genre thành công")
                .build();
    }

    @GetMapping
    ApiResponse<List<GenreResponse>> listGenre() {
        return ApiResponse.<List<GenreResponse>>builder()
                .result(genreService.getGenres())
                .message("Lấy danh sách genre thành công")
                .build();
    }
}
