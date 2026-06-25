package com.example.movie_ticket_be.booking.controller;

import com.example.movie_ticket_be.booking.dto.request.AddFoodsRequest;
import com.example.movie_ticket_be.booking.dto.request.CheckoutRequest;
import com.example.movie_ticket_be.booking.dto.request.InitiateBookingRequest;
import com.example.movie_ticket_be.booking.dto.response.CheckoutResponse;
import com.example.movie_ticket_be.booking.dto.response.InitiateBookingResponse;
import com.example.movie_ticket_be.booking.dto.response.OrderResponse;
import com.example.movie_ticket_be.booking.service.BookingService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/bookings")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BookingController {

	BookingService bookingService;

	@PostMapping("/initiate")
	public ApiResponse<InitiateBookingResponse> initiateBooking(@RequestBody InitiateBookingRequest request) {
		return ApiResponse.<InitiateBookingResponse>builder()
				.code(1000)
				.message("Khóa ghế và tạo đơn hàng thành công")
				.result(bookingService.initiateBooking(request))
				.build();
	}

	@PostMapping("/{orderId}/release")
	public ApiResponse<Void> releaseBooking(@PathVariable Long orderId) {
		bookingService.releaseBooking(orderId);
		return ApiResponse.<Void>builder()
				.code(1000)
				.message("Đã thả ghế và hủy đơn hàng")
				.build();
	}

	@PostMapping("/{orderId}/foods")
	public ApiResponse<OrderResponse> addFoods(@PathVariable Long orderId,
			@RequestBody AddFoodsRequest request) {
		return ApiResponse.<OrderResponse>builder()
				.code(1000)
				.message("Cập nhật đồ ăn thành công")
				.result(bookingService.addFoods(orderId, request))
				.build();
	}

	@PostMapping("/{orderId}/checkout")
	public ApiResponse<CheckoutResponse> checkout(@PathVariable Long orderId,
			@RequestBody CheckoutRequest request,
			HttpServletRequest httpRequest) {
		return ApiResponse.<CheckoutResponse>builder()
				.code(1000)
				.message("Đơn hàng đã được xử lý, chuyển hướng thanh toán")
				.result(bookingService.checkout(orderId, request, httpRequest))
				.build();
	}
}
