package com.example.movie_ticket_be.promotion.mapper;

import com.example.movie_ticket_be.promotion.dto.request.EventRequest;
import com.example.movie_ticket_be.promotion.dto.response.EventResponse;
import com.example.movie_ticket_be.promotion.entity.Event;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mapping(target = "movies", ignore = true)
    @Mapping(target = "eventId", ignore = true)
    @Mapping(target = "entityStatus", ignore = true)
    @Mapping(target = "eventStatus", ignore = true)
    Event toEvent(EventRequest request);

    @Mapping(source = "movies.movieId", target = "movieId")
    EventResponse toEventResponse(Event event);
}
