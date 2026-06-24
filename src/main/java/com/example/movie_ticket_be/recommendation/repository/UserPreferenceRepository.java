package com.example.movie_ticket_be.recommendation.repository;

import com.example.movie_ticket_be.recommendation.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UserPreference.UserPreferenceId>{
}
