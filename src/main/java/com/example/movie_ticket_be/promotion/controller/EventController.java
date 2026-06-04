package com.example.movie_ticket_be.promotion.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.promotion.dto.response.EventDetailrespone;
import com.example.movie_ticket_be.promotion.dto.response.EventResponse;
import com.example.movie_ticket_be.promotion.entity.Event;
import com.example.movie_ticket_be.promotion.service.EventService;
import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EventController {
	EventService eventService;

	@GetMapping
	public ApiResponse<Page<EventResponse>> getActiveEvents(
			@Parameter(name = "filter", required = false) @Filter Specification<Event> spec,
			@ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
		return ApiResponse.<Page<EventResponse>>builder().result(eventService.getActiveEvents(spec, pageable)).build();
	}

	@GetMapping("/{id}")
	public ApiResponse<EventDetailrespone> getEventById(@PathVariable long id) {
		return ApiResponse.<EventDetailrespone>builder().result(eventService.getEventById(id)).build();
	}
}
