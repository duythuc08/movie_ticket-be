package com.example.movie_ticket_be.promotion.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.movie_ticket_be.promotion.entity.Event;

public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
	boolean existsByEventId(Long eventId);

	Optional<Event> findByEventId(Long id);
}
