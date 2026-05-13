package com.example.movie_ticket_be.user.repository;

import com.example.movie_ticket_be.user.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users,String> {
    boolean existsByUsername(String username);
    Optional<Users> findByUsername(String username);
    List<Users> findAllByEnabledFalseAndCreatedAtBefore(LocalDateTime time);

    Optional<Users> findByUserId(String userId);
}
