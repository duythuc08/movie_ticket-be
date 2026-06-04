package com.example.movie_ticket_be.promotion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.promotion.dto.request.EventRequest;
import com.example.movie_ticket_be.promotion.dto.response.AdminEventResponse;
import com.example.movie_ticket_be.promotion.entity.Event;
import com.example.movie_ticket_be.promotion.mapper.EventMapper;
import com.example.movie_ticket_be.promotion.repository.EventRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminEventService {
	EventRepository eventRepository;
	EventMapper eventMapper;
	MovieRepository movieRepository;

	public AdminEventResponse createEvent(EventRequest request) {
		Event event = eventMapper.toEvent(request);
		event.setEntityStatus(EntityStatus.ACTIVE);
		if (request.getMovieId() != null) {
			Movies movie = movieRepository.findByMovieId(request.getMovieId())
					.orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
			event.setMovies(movie);
		}
		return eventMapper.toAdminEventResponse(eventRepository.save(event));
	}

	public Page<AdminEventResponse> getAllEvents(Specification<Event> spec, Pageable pageable) {
		return eventRepository.findAll(spec, pageable).map(eventMapper::toAdminEventResponse);
	}

	public AdminEventResponse getEventById(long id) {
		Event event = eventRepository.findByEventId(id).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
		return eventMapper.toAdminEventResponse(event);
	}

	public AdminEventResponse updateEvent(long id, EventRequest request) {
		Event event = eventRepository.findByEventId(id).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
		event.setTitle(request.getTitle());
		event.setDescription(request.getDescription());
		event.setPosterUrl(request.getPosterUrl());
		event.setStartTime(request.getStartTime());
		event.setEndTime(request.getEndTime());
		event.setEventType(request.getEventType());
		if (request.getMovieId() != null) {
			Movies movie = movieRepository.findByMovieId(request.getMovieId())
					.orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
			event.setMovies(movie);
		} else {
			event.setMovies(null);
		}
		return eventMapper.toAdminEventResponse(eventRepository.save(event));
	}

	public void changeStatus(long id, EntityStatus entityStatus) {
		Event event = eventRepository.findByEventId(id).orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
		event.setEntityStatus(entityStatus);
		eventRepository.save(event);
	}
}
