package com.example.movie_ticket_be.recommendation.service;

import com.example.movie_ticket_be.movie.entity.Reviews;
import com.example.movie_ticket_be.movie.repository.ReviewRepository;
import com.example.movie_ticket_be.recommendation.config.RecommendationProperties;
import com.example.movie_ticket_be.recommendation.entity.UserActivityLog;
import com.example.movie_ticket_be.recommendation.enums.ActionType;
import com.example.movie_ticket_be.recommendation.enums.ParamName;
import com.example.movie_ticket_be.recommendation.repository.ScoringParamRepository;
import com.example.movie_ticket_be.recommendation.repository.UserActivityLogRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
//Tính điểm Utility Matrix Y cho 1 cặp (user, movie)
public class ScoringService {
    final UserActivityLogRepository userActivityLogRepository;
    final ScoringParamRepository scoringParamRepository;
    final ReviewRepository reviewRepository;
    final RecommendationProperties properties;

    //True nếu user đã có đánh giá (review) cho movie -> set has_explicit cho matrix Y
    public boolean hasExplicitRating(String userId, Long movieId) {
        return reviewRepository.findApprovalReview(userId, movieId).isPresent();
    }

    /**
     * y_{u,i} cuối cùng theo has_explicit.
     * TRUE  -> trả thẳng rating thật từ review.
     * FALSE -> tính S qua calculateRawScore(), áp y = 3 + A * tanh(S / S0).
     */
    public BigDecimal calculateY(String userId, Long movieId){
        Optional<Reviews> explicit = reviewRepository.findApprovalReview(userId, movieId);
        if(explicit.isPresent()){
            return BigDecimal.valueOf(explicit.get().getRating());
        }

        double s = calculateRawScore(userId, movieId);
        double s0 = readParam(ParamName.S0, 1.0);
        double amplitude = properties.getTanhConversion().getAmplitude();
        double neutralPoint = properties.getTanhConversion().getNeutralPoint();

        double y = neutralPoint + amplitude * Math.tanh(s / s0);
        return BigDecimal.valueOf(y);
    }

    /**
     * S_{u,i} thô (trước tanh) — Σ [w(a) × decay(Δt)] qua mọi dòng activity log của cặp (user, movie
     *   - Nhóm 1: trọng số cố định (VIEW_SHOWTIMES, SEARCH, SKIP_RECOMMENDATION)
     *   - Nhóm 2: trục Độ sâu, dùng bestValueAt (WATCH_TRAILER, VIEW_DETAILS)
     *   - Nhóm 3: trục Tần suất, dùng occurrenceCount (BOOK_TICKET, SHARE_MOVIE)
     */
    public double calculateRawScore(String userId, Long movieId){
        List<UserActivityLog> logs = userActivityLogRepository.findAllByUser_UserIdAndMovie_MovieId(userId, movieId);

        double alpha = readParam(ParamName.ALPHA, 0.5);
        double now = nowEpochDays();
        double lambda = properties.getDecay().getLambda();

        double total = 0.0;
        for(UserActivityLog log : logs){
            ActionType actionType = log.getUserActivityLogId().getActionType();
            double w = baseWeight(log, actionType);
            double decay = decayFactor(log, actionType, now, lambda);
            double freq = frequencyMultiplier(log, actionType, alpha);
            total += w * decay * freq;
        }
        return total;
    }

    private double readParam(ParamName paramName, double fallback){
        return scoringParamRepository.findById(paramName)
                .map(p -> p.getParamValue().doubleValue())
                .orElse(fallback);
    }

    /** w_base(a) — tra theo MAX value (trục Độ sâu) hoặc base cố định/Tần suất. */
    private double baseWeight(UserActivityLog log, ActionType action) {
        var weights = properties.getWeights();
        switch (action) {
            case VIEW_SHOWTIMES:
                return weights.getViewShowtime();
            case SEARCH:
                return weights.getSearch();
            case SKIP_RECOMMENDATION:
                return weights.getSkipRecommendation();
            case WATCH_TRAILER:
                return watchTrailerWeight(log, weights);
            case VIEW_DETAILS:
                return viewDetailWeight(log, weights);
            case BOOK_TICKET:
                return weights.getBookTicket().getBase();
            case SHARE_MOVIE:
                return weights.getShareMovie().getBase();
            case CANCEL_PAYMENT:
                return weights.getCancelPayment();
            case ABANDON_SEAT_SELECTION:
                return weights.getAbandonSeatSelection();
            case TIMEOUT_HOLD_SEATS:
                return weights.getTimeoutHoldSeat();
            default:
                return 0.0;
        }
    }

    private double watchTrailerWeight(UserActivityLog log, RecommendationProperties.Weights weights) {
        Object pctObj = log.getMetadata() == null ? null : log.getMetadata().get("watch_pct");
        double watchPct = toDouble(pctObj, 0.0);
        var cfg = weights.getWatchTrailer();
        if (watchPct > cfg.getHighThreshold()) return cfg.getHigh();
        if (watchPct >= cfg.getMediumThreshold()) return cfg.getMedium();
        return cfg.getLow();
    }

    private double viewDetailWeight(UserActivityLog log, RecommendationProperties.Weights weights) {
        Object durObj = log.getMetadata() == null ? null : log.getMetadata().get("duration_sec");
        double durationSec = toDouble(durObj, 0.0);
        var cfg = weights.getViewDetail();
        if (durationSec > cfg.getHighThreshold()) return cfg.getHigh();
        if (durationSec >= cfg.getLowThreshold()) return cfg.getMid();
        return cfg.getLow();
    }

    /**
     * decay(Δt) = e^(-lambda × Δt). Δt tính theo bestValueAt cho trục Độ sâu
     * (mốc thời gian đạt giá trị MAX), theo updatedAt cho trục Tần suất và
     * nhóm cố định (mốc lần tương tác gần nhất).
     */
    private double decayFactor(UserActivityLog log, ActionType action, double nowEpochDays, double lambda) {
        LocalDateTime referenceTime = isDepthAxis(action)
                ? (log.getBestValueAt() != null ? log.getBestValueAt() : log.getUpdatedAt())
                : log.getUpdatedAt();

        if (referenceTime == null) return 1.0; // an toàn nếu thiếu mốc thời gian
        double deltaDays = nowEpochDays - epochDays(referenceTime);
        return Math.exp(-lambda * Math.max(deltaDays, 0));
    }

    /**
     * c(a) = 1 + alpha × occurrenceCount (Hu, Koren, Volinsky 2008) — CHỈ áp
     * dụng cho trục Tần suất (BOOK_TICKET, SHARE_MOVIE). Nhóm khác trả 1.0
     * (không khuếch đại theo số lần lặp).
     */
    private double frequencyMultiplier(UserActivityLog log, ActionType action, double alpha) {
        if (!isFrequencyAxis(action)) return 1.0;
        int count = log.getOccurrenceCount() == null ? 1 : log.getOccurrenceCount();
        return 1.0 + alpha * count;
    }

    private boolean isDepthAxis(ActionType action) {
        return action == ActionType.WATCH_TRAILER || action == ActionType.VIEW_DETAILS;
    }

    private boolean isFrequencyAxis(ActionType action) {
        return action == ActionType.BOOK_TICKET
                || action == ActionType.SHARE_MOVIE;
    }

    private double toDouble(Object value, double fallback) {
        if (value == null) return fallback;
        if (value instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(value.toString());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }


    private double nowEpochDays() {
        return epochDays(LocalDateTime.now());
    }

    private double epochDays(LocalDateTime time) {
        return time.toLocalDate().toEpochDay()
                + time.toLocalTime().toSecondOfDay() / 86400.0;
    }

}