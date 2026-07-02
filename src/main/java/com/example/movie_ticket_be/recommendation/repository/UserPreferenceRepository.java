package com.example.movie_ticket_be.recommendation.repository;

import com.example.movie_ticket_be.recommendation.entity.UserPreference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UserPreference.UserPreferenceId>{
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO user_preference (user_id, movie_id, predicted_score, neighbor_count, created_at, updated_at, entity_status)
            VALUES (:userId, :movieId, :predictedScore, :neighborCount, NOW(), NOW(), 'ACTIVE')
            ON DUPLICATE KEY UPDATE
                predicted_score = :predictedScore,
                neighbor_count = :neighborCount,
                updated_at = NOW()
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
     * Đọc top-N preference của user, JOIN FETCH movie để tránh N+1 khi lấy title/posterUrl.
     * Dùng Pageable để giới hạn số dòng theo prediction.top-n trong config.
     */
    @Query("""
            SELECT p FROM UserPreference p
            JOIN FETCH p.movie
            WHERE p.preferenceId.userId = :userId
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
