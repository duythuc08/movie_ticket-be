package com.example.movie_ticket_be.promotion.mapper;

import com.example.movie_ticket_be.promotion.dto.request.EventRequest;
import com.example.movie_ticket_be.promotion.dto.response.AdminEventResponse;
import com.example.movie_ticket_be.promotion.entity.Event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.example.movie_ticket_be.promotion.dto.response.EventDetailrespone;
import com.example.movie_ticket_be.promotion.dto.response.EventResponse;

@Mapper(componentModel = "spring")
public interface EventMapper {

	@Mapping(target = "movies", ignore = true)
	@Mapping(target = "eventId", ignore = true)
	@Mapping(target = "entityStatus", ignore = true)
	@Mapping(target = "eventStatus", ignore = true)
	Event toEvent(EventRequest request);

	@Mapping(source = "movies.movieId", target = "movieId")
	AdminEventResponse toAdminEventResponse(Event event);

	EventResponse toEventResponse(Event event);

	@Mapping(source = "movies.title", target = "movieTitle")
	EventDetailrespone toEventDetailResponse(Event event);
}
