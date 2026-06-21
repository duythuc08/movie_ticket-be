package com.example.movie_ticket_be.cf.repository;

import com.example.movie_ticket_be.cf.entity.UserPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserPreferenceRepository extends JpaRepository<UserPreference, UserPreference.UserPreferenceId>{
}
