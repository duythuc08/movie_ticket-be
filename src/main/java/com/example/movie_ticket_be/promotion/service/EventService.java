package com.example.movie_ticket_be.promotion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.promotion.dto.response.EventDetailrespone;
import com.example.movie_ticket_be.promotion.dto.response.EventResponse;
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
public class EventService {
    EventRepository eventRepository;
    EventMapper eventMapper;

    public Page<EventResponse> getActiveEvents(Specification<Event> spec, Pageable pageable) {
        return eventRepository.findAll(spec, pageable).map(eventMapper::toEventResponse);
    }

    public EventDetailrespone getEventById(long id) {
        return eventMapper.toEventDetailResponse(
                eventRepository.findByEventId(id)
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND)));
    }
}
