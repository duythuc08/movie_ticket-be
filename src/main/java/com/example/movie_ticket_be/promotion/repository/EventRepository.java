package com.example.movie_ticket_be.promotion.repository;

import com.example.movie_ticket_be.promotion.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EventRepository extends JpaRepository<Event, String> {
    boolean existsByEventId(Long eventId);

    Optional<Event> findByEventId(Long id);
}
