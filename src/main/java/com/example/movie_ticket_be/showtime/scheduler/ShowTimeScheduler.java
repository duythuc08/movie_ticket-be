package com.example.movie_ticket_be.showtime.scheduler;

import com.example.movie_ticket_be.showtime.service.ShowTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShowTimeScheduler {

    private final ShowTimeService showTimeService;

    @Scheduled(fixedDelay = 60000)
    public void autoUpdateShowTimeStatus() {
        log.info("Running autoUpdateShowTimeStatus...");
        showTimeService.autoUpdateShowTimeStatus();
    }
}