package com.example.movie_ticket_be.recommendation.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "recommendation")
public class RecommendationProperties {

    private Weights weights = new Weights();
    private Decay decay = new Decay();
    private TanhConversion tanhConversion = new TanhConversion();
    private Cf cf = new Cf();
    private ColdStart coldStart = new ColdStart();

    @Data
    public static class Weights {
        private int viewShowtime;
        private int search;
        private int skipRecommendation;
        private int chainViewShowtimeThenBook;
        private int chainWindowMinutes;

        private int cancelPayment;
        private int abandonSeatSelection;
        private int timeoutHoldSeat;

        private WatchTrailer watchTrailer = new WatchTrailer();
        private ViewDetail viewDetail = new ViewDetail();
        private BookTicket bookTicket = new BookTicket();
        private ShareMovie shareMovie = new ShareMovie();

        @Data
        public static class WatchTrailer {
            private int high;
            private int medium;
            private int low;
            private double highThreshold;
            private double mediumThreshold;
        }

        @Data
        public static class ViewDetail {
            private int high;
            private int mid;
            private int low;
            private double highThreshold;
            private double lowThreshold;
        }

        @Data
        public static class BookTicket {
            private int base;
        }

        @Data
        public static class ShareMovie {
            private int base;
        }
    }

    @Data
    public static class Decay {
        private double lambda;
    }

    @Data
    public static class TanhConversion {
        private double amplitude;
        private double neutralPoint;
    }

    @Data
    public static class Cf {
        private int topK;
        private int minCoRatedItems;
        private double minSimilarity;
    }

    @Data
    public static class ColdStart {
        private int minInteractionsThreshold;
        private double popularityAlpha;
    }
}
