package com.example.movie_ticket_be.recommendation.service;

import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.recommendation.dto.request.ActivityLogRequest;
import com.example.movie_ticket_be.recommendation.entity.UserActivityLog;
import com.example.movie_ticket_be.recommendation.enums.ActionType;
import com.example.movie_ticket_be.recommendation.repository.UserActivityLogRepository;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.user.entity.Users;
import com.example.movie_ticket_be.user.repository.UserRepository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserActivityLogService {

    UserActivityLogRepository userActivityLogRepository;
    MovieRepository movieRepository;
    UserRepository userRepository;

    static final Set<ActionType> FE_CALLABLE_ACTIONS = EnumSet.of(
            ActionType.WATCH_TRAILER,
            ActionType.VIEW_DETAILS,
            ActionType.VIEW_SHOWTIMES,
            ActionType.SEARCH,
            ActionType.SHARE_MOVIE,
            ActionType.SKIP_RECOMMENDATION,
            ActionType.ABANDON_SEAT_SELECTION
    );

    // So sánh MAX — cập nhật metadata + bestValueAt khi giá trị mới lớn hơn hoặc bằng
    static final Set<ActionType> DEPTH_AXIS_ACTIONS = EnumSet.of(
            ActionType.WATCH_TRAILER,
            ActionType.VIEW_DETAILS
    );

    @Async
    public void logFromFrontend(ActivityLogRequest request, String userId) {
        if (!FE_CALLABLE_ACTIONS.contains(request.getActionType())) {
            log.warn("Blocked FE attempt to log internal action: {}", request.getActionType());
            return;
        }
        Users user = userRepository.findByUserId(userId).orElse(null);
        if (user == null) {
            log.warn("UserActivityLog skipped: user not found [userId={}]", userId);
            return;
        }
        doLog(request, user);
    }

    @Async
    public void logInternal(ActivityLogRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Users user = userRepository.findByUsername(username).orElse(null);
        if (user == null) {
            log.warn("UserActivityLog skipped: user not found [username={}]", username);
            return;
        }
        doLog(request, user);
    }

    @Async
    public void logInternal(ActivityLogRequest request, Users user) {
        doLog(request, user);
    }

    private void doLog(ActivityLogRequest request, Users user) {
        try {
            if (request.getMovieId() == null) {
                log.warn("Activity rejected: movieId is required [user={}, action={}]",
                        user.getUserId(), request.getActionType());
                return;
            }

            Movies movie = movieRepository.findById(request.getMovieId()).orElse(null);
            if (movie == null) {
                log.warn("Activity skipped: movie not found [movieId={}]", request.getMovieId());
                return;
            }

            UserActivityLog.UserActivityLogId id = UserActivityLog.UserActivityLogId.builder()
                    .userId(user.getUserId())
                    .movieId(request.getMovieId())
                    .actionType(request.getActionType())
                    .build();

            UserActivityLog existing = userActivityLogRepository.findById(id).orElse(null);

            if (existing == null) {
                LocalDateTime bestValueAt = DEPTH_AXIS_ACTIONS.contains(request.getActionType())
                        ? LocalDateTime.now()
                        : null;

                userActivityLogRepository.save(UserActivityLog.builder()
                        .userActivityLogId(id)
                        .user(user)
                        .movie(movie)
                        .metadata(request.getMetadata())
                        .occurrenceCount(1)
                        .bestValueAt(bestValueAt)
                        .build());

            } else {
                existing.setOccurrenceCount(
                        (existing.getOccurrenceCount() == null ? 0 : existing.getOccurrenceCount()) + 1
                );

                ActionType actionType = request.getActionType();
                if (actionType == ActionType.WATCH_TRAILER) {
                    //cập nhật khi watch_pct mới >= cũ
                    applyDepthAxis(existing, request.getMetadata(), "watch_pct");
                } else if (actionType == ActionType.VIEW_DETAILS) {
                    //cập nhật khi duration_sec mới >= cũ
                    applyDepthAxis(existing, request.getMetadata(), "duration_sec");
                } else {
                    // Trục tần suất (BOOK_TICKET, SHARE_MOVIE) + nhóm còn lại:
                    // ghi đè metadata bằng giá trị mới nhất
                    existing.setMetadata(request.getMetadata());
                }

                userActivityLogRepository.save(existing);
            }

            log.debug("Upserted activity [user={}, action={}, movie={}]",
                    user.getUserId(), request.getActionType(), request.getMovieId());

        } catch (Exception e) {
            log.error("Failed to log activity [user={}, action={}]: {}",
                    user.getUserId(), request.getActionType(), e.getMessage());
        }
    }

    /**
     * Ghi đè metadata + bestValueAt chỉ khi giá trị tại metaKey mới >= cũ.
     * Nếu nhỏ hơn: giữ nguyên metadata và bestValueAt —
     */
    private void applyDepthAxis(UserActivityLog existing,
                                 Map<String, Object> newMeta,
                                 String metaKey) {
        double newVal = getNumericMeta(newMeta, metaKey);
        double oldVal = getNumericMeta(existing.getMetadata(), metaKey);
        if (newVal >= oldVal) {
            existing.setMetadata(newMeta);
            existing.setBestValueAt(LocalDateTime.now());
        }
    }

    private double getNumericMeta(Map<String, Object> meta, String key) {
        if (meta == null) return 0.0;
        Object val = meta.get(key);
        return val instanceof Number ? ((Number) val).doubleValue() : 0.0;
    }
}
