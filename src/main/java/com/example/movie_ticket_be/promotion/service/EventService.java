package com.example.movie_ticket_be.promotion.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.promotion.dto.response.EventResponse;
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
public class EventService {
    EventRepository eventRepository;
    EventMapper eventMapper;

    public List<EventResponse> getActiveEvents() {
        return eventRepository.findAll().stream()
                .filter(e -> e.getEntityStatus() == EntityStatus.ACTIVE)
                .map(eventMapper::toEventResponse)
                .toList();
    }

    public EventResponse getEventById(long id) {
        return eventMapper.toEventResponse(
                eventRepository.findByEventId(id)
                        .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND)));
    }
}
