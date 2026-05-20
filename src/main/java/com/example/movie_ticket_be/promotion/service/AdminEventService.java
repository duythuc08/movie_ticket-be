package com.example.movie_ticket_be.promotion.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.promotion.dto.request.EventRequest;
import com.example.movie_ticket_be.promotion.dto.response.EventResponse;
import com.example.movie_ticket_be.promotion.entity.Event;
import com.example.movie_ticket_be.promotion.mapper.EventMapper;
import com.example.movie_ticket_be.promotion.repository.EventRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminEventService {
    EventRepository eventRepository;
    EventMapper eventMapper;
    MovieRepository movieRepository;

    public EventResponse createEvent(EventRequest request) {
        Event event = eventMapper.toEvent(request);
        event.setEntityStatus(EntityStatus.ACTIVE);
        if (request.getMovieId() != null) {
            Movies movie = movieRepository.findByMovieId(request.getMovieId())
                    .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
            event.setMovies(movie);
        }
        return eventMapper.toEventResponse(eventRepository.save(event));
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(eventMapper::toEventResponse)
                .toList();
    }

    public EventResponse getEventById(long id) {
        Event event = eventRepository.findByEventId(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        return eventMapper.toEventResponse(event);
    }

    public void changeStatus(long id, EntityStatus entityStatus) {
        Event event = eventRepository.findByEventId(id)
                .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
        event.setEntityStatus(entityStatus);
        eventRepository.save(event);
    }
}
