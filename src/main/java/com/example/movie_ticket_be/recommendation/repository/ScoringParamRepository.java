package com.example.movie_ticket_be.recommendation.repository;

import com.example.movie_ticket_be.recommendation.entity.ScoringParam;
import com.example.movie_ticket_be.recommendation.enums.ParamName;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScoringParamRepository extends JpaRepository<ScoringParam, ParamName> {
}
