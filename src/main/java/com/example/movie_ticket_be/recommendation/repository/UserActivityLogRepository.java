package com.example.movie_ticket_be.recommendation.repository;

import com.example.movie_ticket_be.recommendation.entity.UserActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserActivityLogRepository
        extends JpaRepository<UserActivityLog, UserActivityLog.UserActivityLogId>,
                JpaSpecificationExecutor<UserActivityLog> {


    /**
     * Toàn bộ dòng log (mọi action_type) của 1 cặp (user, movie) — dùng trong
     * ScoringService.calculateRawScore() để cộng Σ w(a) × decay(Δt) qua mọi action.
     */

    List<UserActivityLog> findAllByUser_UserIdAndMovie_MovieId(String userUserId, Long movieMovieId);

    @Query(value = "SELECT * FROM user_activity_logs WHERE user_id = :userId", nativeQuery = true)
    List<UserActivityLog> findAllByUserId(@Param("userId") String userId);


    // dùng để tính median(occurrence_count) -> alpha = 1 / median (Hu et al. 2008).
    @Query(value = """
            SELECT occurrence_count FROM user_activity_logs
            WHERE action_type IN ('BOOK_TICKET', 'SHARE_MOVIE')
              AND occurrence_count >= 2
            ORDER BY occurrence_count
            """, nativeQuery = true)

    List<Integer> findOccurrenceCountsForFrequencyAxis();


    /**
     * Toàn bộ log của các cặp (user, movie) CHƯA có review thật — dùng làm input
     * cho ScoringService.calculateRawScore() khi ước lượng S0.
     * */
    @Query(value = """
        SELECT l.* FROM user_activity_logs l
            WHERE NOT EXISTS (
                SELECT 1 FROM review r
                WHERE r.user_id = l.user_id
                  AND r.movie_id = l.movie_id
                  AND r.review_status = 'APPROVED'
            )
            ORDER BY l.user_id, l.movie_id
    """, nativeQuery = true)
    List<UserActivityLog> findLogWithoutExplicitRating();

    @Query(value = """
            SELECT DISTINCT user_id, movie_id FROM user_activity_logs l
            WHERE NOT EXISTS (
                SELECT 1 FROM review r
                WHERE r.user_id = l.user_id
                  AND r.movie_id = l.movie_id
                  AND r.review_status = 'APPROVED'
            )
    """, nativeQuery = true)
    List<Object[]> findUserMoviePairsWithoutExplicitRating();
}
