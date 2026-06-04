package com.example.movie_ticket_be.booking.controller;

import com.example.movie_ticket_be.booking.dto.response.AdminOrderStatsResponse;
import com.example.movie_ticket_be.booking.dto.response.AdminOrderSummaryResponse;
import com.example.movie_ticket_be.booking.dto.response.OrderResponse;
import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.booking.service.AdminOrderService;
import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.turkraft.springfilter.boot.Filter;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

	AdminOrderService adminOrderService;

	@GetMapping
	public ApiResponse<Page<AdminOrderSummaryResponse>> getOrders(
			@Parameter(name = "filter", required = false) @Filter Specification<Orders> spec,
			@ParameterObject @PageableDefault(sort = "bookingTime", direction = Sort.Direction.DESC, size = 10) Pageable pageable) {

		return ApiResponse.<Page<AdminOrderSummaryResponse>>builder()
				.result(adminOrderService.getOrders(spec, pageable)).build();
	}

	@GetMapping("/{orderId}")
	public ApiResponse<OrderResponse> getOrderDetail(@PathVariable Long orderId) {
		return ApiResponse.<OrderResponse>builder().result(adminOrderService.getOrderDetail(orderId)).build();
	}

	@PostMapping("/{orderId}/checkin")
	public ApiResponse<String> checkin(@PathVariable Long orderId, @RequestParam String qrCode) {
		adminOrderService.checkin(orderId, qrCode);
		return ApiResponse.<String>builder().message("Check-in thành công").result("OK").build();
	}

	@GetMapping("/stats")
	public ApiResponse<AdminOrderStatsResponse> getStats(
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {

		LocalDateTime fromDt = from != null ? from.atStartOfDay() : LocalDate.now().minusMonths(1).atStartOfDay();
		LocalDateTime toDt = to != null ? to.atTime(23, 59, 59) : LocalDate.now().atTime(23, 59, 59);

		return ApiResponse.<AdminOrderStatsResponse>builder().result(adminOrderService.getStats(fromDt, toDt)).build();
	}
}
