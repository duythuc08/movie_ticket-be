package com.example.movie_ticket_be.recommendation.repository.projection;

public interface GenreProfileRow {
    String getGenreName();
    Long getLikedCount();
    Double getWeightPct();
}
