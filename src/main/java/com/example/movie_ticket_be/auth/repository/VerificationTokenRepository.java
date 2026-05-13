package com.example.movie_ticket_be.auth.repository;

import com.example.movie_ticket_be.auth.entity.VerificationToken;
import com.example.movie_ticket_be.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationTokenRepository extends JpaRepository<VerificationToken,String> {

    Optional<VerificationToken> findByVerificationCode(String verificationCode);
    Optional<VerificationToken> findByUser(Users user);
    Optional<VerificationToken> findByUserAndVerificationCode(Users user, String verificationCode);

    void deleteByUser(Users user);
}
