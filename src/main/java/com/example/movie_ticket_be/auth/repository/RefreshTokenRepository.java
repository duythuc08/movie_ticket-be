package com.example.movie_ticket_be.auth.repository;

import com.example.movie_ticket_be.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {

	@Modifying
	@Transactional
	@Query("DELETE FROM RefreshToken rt WHERE rt.expiryTime < :now")
	void deleteExpiredTokens(Date now);
}
