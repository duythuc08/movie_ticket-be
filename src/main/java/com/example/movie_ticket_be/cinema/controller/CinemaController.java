package com.example.movie_ticket_be.cinema.controller;

import com.example.movie_ticket_be.cinema.dto.response.CinemaResponse;
import com.example.movie_ticket_be.cinema.service.CinemaService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/cinemas")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CinemaController {
	CinemaService cinemaService;

	@GetMapping("/getCinemas")
	public ApiResponse<List<CinemaResponse>> getCinemas() {
		return ApiResponse.<List<CinemaResponse>>builder().result(cinemaService.getCinemas()).build();
	}

	@GetMapping("/getCinema/{id}")
	public ApiResponse<CinemaResponse> getCinemaById(@PathVariable Long id) {
		return ApiResponse.<CinemaResponse>builder().result(cinemaService.getCinemaById(id)).build();
	}
}
