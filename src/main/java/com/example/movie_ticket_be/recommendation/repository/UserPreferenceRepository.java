package com.example.movie_ticket_be.recommendation.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.example.movie_ticket_be.recommendation.entity.UserPreference;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UserPreference.UserPreferenceId>{
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO user_preference (user_id, movie_id, predicted_score, neighbor_count)
            VALUES (:userId, :movieId, :predictedScore, :neighborCount)
            ON DUPLICATE KEY UPDATE
                predicted_score = :predictedScore,
                neighbor_count = :neighborCount
            """, nativeQuery = true)
    void upsert(@Param("userId") String userId,
                @Param("movieId") Long movieId,
                @Param("predictedScore") BigDecimal predictedScore,
                @Param("neighborCount") int neighborCount);

    @Query("""
            SELECT p FROM UserPreference p
            WHERE p.preferenceId.userId = :userId
            ORDER BY p.predictedScore DESC
            """)
    List<UserPreference> findTopByUserId(@Param("userId") String userId);

    /**
     * Đọc top-N preference của user, loại phim user đã đánh giá hoặc đã đặt vé.
     * Lọc real-time tại DB để tránh gợi ý phim user đã tương tác sau lần batch 3AM.
     */
    @Query("""
            SELECT p FROM UserPreference p
            JOIN FETCH p.movie
            WHERE p.preferenceId.userId = :userId
              AND p.source IS NOT NULL
              AND NOT EXISTS (
                  SELECT 1 FROM Reviews r
                  WHERE r.users.userId = :userId
                    AND r.movies.movieId = p.preferenceId.movieId
                    AND r.reviewStatus = 'APPROVED'
              )
              AND NOT EXISTS (
                  SELECT 1 FROM UserActivityLog l
                  WHERE l.user.userId = :userId
                    AND l.movie.movieId = p.preferenceId.movieId
                    AND l.userActivityLogId.actionType = 'BOOK_TICKET'
                    AND l.entityStatus = 'ACTIVE'
              )
            ORDER BY p.predictedScore DESC
            """)
    List<UserPreference> findTopByUserIdFetchMovie(@Param("userId") String userId, Pageable pageable);

    /**
     * Xóa toàn bộ dòng cũ của user trước khi UPSERT lượt predict mới — tránh
     * trường hợp phim từng nằm trong top-10 lượt trước nhưng KHÔNG còn trong
     * candidate set lượt này (vd: user vừa đặt vé phim đó) vẫn còn sót lại
     * trong bảng vì ON DUPLICATE KEY UPDATE chỉ cập nhật phim CÓ trong batch
     * mới, không tự xóa phim không còn xuất hiện.
     */
    @Modifying
    @Transactional
    @Query(value = "DELETE FROM user_preference WHERE user_id = :userId", nativeQuery = true)
    void deleteAllByUserId(@Param("userId") String userId);
}
