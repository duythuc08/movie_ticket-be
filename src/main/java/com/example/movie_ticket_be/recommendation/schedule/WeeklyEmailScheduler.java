package com.example.movie_ticket_be.recommendation.schedule;

import com.example.movie_ticket_be.recommendation.service.WeeklyRecommendationEmailService;
import com.example.movie_ticket_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WeeklyEmailScheduler {

    private final UserRepository userRepository;
    private final WeeklyRecommendationEmailService weeklyEmailService;

    @Scheduled(cron = "0 0 8 * * MON")
    public void sendWeeklyRecommendationEmails() {
        log.info("[WeeklyEmail] Bắt đầu gửi mail tuần theo lịch...");
        weeklyEmailService.sendToAllUsers(userRepository.findAllByEnabledTrue());
    }
}
