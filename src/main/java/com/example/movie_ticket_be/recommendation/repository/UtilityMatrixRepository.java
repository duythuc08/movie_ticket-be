package com.example.movie_ticket_be.recommendation.repository;

import com.example.movie_ticket_be.recommendation.entity.UtilityMatrix;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UtilityMatrixRepository extends JpaRepository<UtilityMatrix, UtilityMatrix.UtilityMatrixId> {

    @Query("SELECT u FROM UtilityMatrix u WHERE u.matrixId.userId = :userId")
    List<UtilityMatrix> findAllByUserId(String userId);

    @Query(value = """
            INSERT INTO utility_matrix (user_id, movie_id, yscore, has_explicit, created_at, updated_at,entity_status)
            VALUES (:userId, :movieId, :yscore, :hasExplicit, NOW(), NOW(), 'ACTIVE')
            ON DUPLICATE KEY UPDATE yscore = :yscore, has_explicit = :hasExplicit, updated_at = NOW()
            """, nativeQuery = true)
    void upsert(@Param("userId") String userId, @Param("movieId") Long movieId,
                @Param("yscore") BigDecimal yscore, @Param("hasExplicit") Boolean hasExplicit);

    /**
     * Danh sách userId eligible cho CF — K_{u,i} >= minInteractions, với K_{u,i} =
     * số PHIM DISTINCT user có tương tác (rating thật HOẶC ít nhất 1 activity log),
     * với minInteractions là tham số đầu vào.
     */
    @Query(value = """
            SELECT u.user_id FROM users u
            WHERE (
                SELECT COUNT(DISTINCT movie_id) FROM (
                    SELECT r.movie_id FROM review r
                    WHERE r.user_id = u.user_id AND r.review_status = 'APPROVED'
                    UNION
                    SELECT l.movie_id FROM user_activity_logs l
                    WHERE l.user_id = u.user_id AND l.action_type != 'WRITE_REVIEW'
                ) AS interacted
            ) >= :minInteractions
            """, nativeQuery = true)
    List<String> findEligibleUserIds(@Param("minInteractions") int minInteractions);

    /**
     * Toàn bộ movieId mà user này từng tương tác (có review APPROVED HOẶC có
     * ít nhất 1 dòng activity log) — dùng làm danh sách (user, movie) cần
     * build Y trong UtilityMatrixBuilderStep.
     */
    @Query(value = """
            SELECT DISTINCT movie_id FROM (
                SELECT movie_id FROM review WHERE user_id = :userId AND review_status = 'APPROVED'
                UNION
                SELECT movie_id FROM user_activity_logs WHERE user_id = :userId
            ) AS interacted
            """, nativeQuery = true)
    List<Long> findInteractedMovieIds(@Param("userId") String userId);

}