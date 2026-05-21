package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.response.GenreResponse;
import com.example.movie_ticket_be.movie.service.GenreService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/genre")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GenreController {
    GenreService genreService;

    @GetMapping
    public ApiResponse<List<GenreResponse>> listGenre() {
        return ApiResponse.<List<GenreResponse>>builder()
                .result(genreService.getGenres())
                .message("Lấy danh sách genre thành công")
                .build();
    }
    
}
