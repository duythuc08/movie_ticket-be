package com.example.movie_ticket_be.user.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.example.movie_ticket_be.user.entity.Users;

public interface UserRepository extends JpaRepository<Users, String>, JpaSpecificationExecutor<Users> {
	boolean existsByUsername(String username);

	boolean existsByPhoneNumber(String phoneNumber);
	Optional<Users> findByUsername(String username);
	List<Users> findAllByEnabledFalseAndCreatedAtBefore(LocalDateTime time);
	List<Users> findAllByEnabledTrue();

	Optional<Users> findByUserId(String userId);
}
