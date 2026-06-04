package com.example.movie_ticket_be.promotion.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.promotion.dto.request.EventRequest;
import com.example.movie_ticket_be.promotion.dto.response.AdminEventResponse;
import com.example.movie_ticket_be.promotion.entity.Event;
import com.example.movie_ticket_be.promotion.service.AdminEventService;
import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/admin/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminEventController {
	AdminEventService adminEventService;

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminEventResponse> createEvent(@RequestBody EventRequest request) {
		return ApiResponse.<AdminEventResponse>builder().result(adminEventService.createEvent(request))
				.message("Tạo sự kiện thành công").build();
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Page<AdminEventResponse>> getAllEvents(
			@Parameter(name = "filter", required = false) @Filter Specification<Event> spec,
			@ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
		return ApiResponse.<Page<AdminEventResponse>>builder().result(adminEventService.getAllEvents(spec, pageable))
				.build();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminEventResponse> getEventById(@PathVariable long id) {
		return ApiResponse.<AdminEventResponse>builder().result(adminEventService.getEventById(id)).build();
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminEventResponse> updateEvent(@PathVariable long id, @RequestBody EventRequest request) {
		return ApiResponse.<AdminEventResponse>builder().result(adminEventService.updateEvent(id, request))
				.message("Cập nhật sự kiện thành công").build();
	}

	@PutMapping("/{id}/activate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> activate(@PathVariable long id) {
		adminEventService.changeStatus(id, EntityStatus.ACTIVE);
		return ApiResponse.<Void>builder().message("Kích hoạt sự kiện thành công").build();
	}

	@PutMapping("/{id}/inactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<Void> inactivate(@PathVariable long id) {
		adminEventService.changeStatus(id, EntityStatus.INACTIVE);
		return ApiResponse.<Void>builder().message("Vô hiệu hóa sự kiện thành công").build();
	}
}
