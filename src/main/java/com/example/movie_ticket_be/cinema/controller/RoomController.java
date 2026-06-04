package com.example.movie_ticket_be.cinema.controller;

import com.example.movie_ticket_be.cinema.dto.response.RoomResponse;
import com.example.movie_ticket_be.cinema.enums.RoomStatus;
import com.example.movie_ticket_be.cinema.enums.RoomType;
import com.example.movie_ticket_be.cinema.service.RoomService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoomController {
	RoomService roomService;

	@GetMapping("/getRooms")
	public ApiResponse<List<RoomResponse>> getAllRooms() {
		return ApiResponse.<List<RoomResponse>>builder().result(roomService.getRooms()).build();
	}

	@GetMapping("/getRooms/by-cinema/{cinemaId}")
	public ApiResponse<List<RoomResponse>> getRoomByCinemaId(@PathVariable Long cinemaId) {
		return ApiResponse.<List<RoomResponse>>builder().result(roomService.getRoomsByCinemaId(cinemaId)).build();
	}

	@GetMapping("/getRooms/by-cinema/{cinemaId}/status")
	public ApiResponse<List<RoomResponse>> getRoomByCinemaIdAndStatus(@PathVariable Long cinemaId,
			@RequestParam RoomStatus status) {
		return ApiResponse.<List<RoomResponse>>builder()
				.result(roomService.getRoomsByCinemaIdAndStatus(cinemaId, status)).build();
	}

	@GetMapping("/getRooms/by-cinema/{cinemaId}/type")
	public ApiResponse<List<RoomResponse>> getRoomByCinemaIdAndType(@PathVariable Long cinemaId,
			@RequestParam RoomType type) {
		return ApiResponse.<List<RoomResponse>>builder().result(roomService.getRoomsByCinemaIdAndType(cinemaId, type))
				.build();
	}
}
