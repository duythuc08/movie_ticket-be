package com.example.movie_ticket_be.cf.repository;

import com.example.movie_ticket_be.cf.entity.UtilityMatrix;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UtilityMatrixRepository extends JpaRepository<UtilityMatrix, UtilityMatrix.UtilityMatrixId> {
}
