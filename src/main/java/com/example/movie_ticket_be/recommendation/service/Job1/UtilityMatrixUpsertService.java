package com.example.movie_ticket_be.recommendation.service.Job1;

import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.recommendation.entity.UserActivityLog;
import com.example.movie_ticket_be.recommendation.repository.UtilityMatrixRepository;
import com.example.movie_ticket_be.recommendation.service.ScoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Tách việc upsert utility_matrix ra khỏi transaction của Tasklet.
 * REQUIRES_NEW đảm bảo mỗi user commit độc lập — nếu JVM crash giữa chừng
 * thì chỉ mất user hiện tại, không rollback toàn bộ batch.
 */
@Service
@RequiredArgsConstructor
public class UtilityMatrixUpsertService {

    private final UtilityMatrixRepository utilityMatrixRepository;
    private final ScoringService scoringService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int upsertForUser(String userId,
                             List<Long> movieIds,
                             Map<Long, Reviews> reviewsByMovie,
                             Map<Long, List<UserActivityLog>> logsByMovie) {
        for (Long movieId : movieIds) {
            ScoringService.YResult r = scoringService.computeYFromPreloaded(movieId, reviewsByMovie, logsByMovie);
            utilityMatrixRepository.upsert(userId, movieId, r.yScore(), r.hasExplicit(), r.hasImplicit());
        }
        return movieIds.size();
    }
}
