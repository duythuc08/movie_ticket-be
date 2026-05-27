package com.example.movie_ticket_be.movie.scheduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.movie_ticket_be.movie.service.AdminMovieService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class MovieScheduler {
    private final AdminMovieService adminMovieService;
    
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void updateMovieStatuses() {
        adminMovieService.updateMovieStatuses();
    }
}
