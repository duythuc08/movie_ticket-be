package com.example.movie_ticket_be.showtime.controller;

import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.showtime.dto.response.SeatSelectionResponse;
import com.example.movie_ticket_be.showtime.service.SeatShowTimeService;
import com.example.movie_ticket_be.showtime.sse.SeatSseManager;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/seatShowTimes")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SeatShowTImeController {
	SeatShowTimeService seatShowTimeService;
	SeatSseManager seatSseManager;

	@GetMapping("/selection/{showTimeId}")
	@PreAuthorize("isAuthenticated()")
	public ApiResponse<SeatSelectionResponse> getSeatSelection(@PathVariable Long showTimeId) {
		return ApiResponse.<SeatSelectionResponse>builder().result(seatShowTimeService.getSeatSelectionData(showTimeId))
				.build();
	}

	// Endpoint SSE — client subscribe để nhận cập nhật ghế real-time.
	// Không dùng @PreAuthorize vì EventSource không gửi được Authorization header;
	// token được xác thực ở tầng SecurityConfig qua query param.
	@GetMapping(value = "/selection/{showTimeId}/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public SseEmitter streamSeatUpdates(@PathVariable Long showTimeId) {
		return seatSseManager.subscribe(showTimeId);
	}
}
