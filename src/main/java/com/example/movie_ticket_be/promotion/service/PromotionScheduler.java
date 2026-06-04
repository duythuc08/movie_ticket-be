package com.example.movie_ticket_be.promotion.service;

import com.example.movie_ticket_be.promotion.entity.Promotion;
import com.example.movie_ticket_be.promotion.enums.PromotionStatus;
import com.example.movie_ticket_be.promotion.repository.PromotionRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PromotionScheduler {

    PromotionRepository promotionRepository;

    @Scheduled(cron = "0 0 * * * *")
    public void autoExpirePromotions() {
        List<Promotion> expired = promotionRepository
                .findByStatusAndEndTimeBefore(PromotionStatus.PUBLISHED, LocalDateTime.now());
        if (!expired.isEmpty()) {
            expired.forEach(p -> p.setStatus(PromotionStatus.EXPIRED));
            promotionRepository.saveAll(expired);
            log.info("Auto-expired {} promotion(s)", expired.size());
        }
    }
}
