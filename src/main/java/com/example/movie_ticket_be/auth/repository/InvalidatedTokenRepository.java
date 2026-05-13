package com.example.movie_ticket_be.auth.repository;


import com.example.movie_ticket_be.auth.entity.InvalidatedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvalidatedTokenRepository extends JpaRepository<InvalidatedToken, String> {
}
