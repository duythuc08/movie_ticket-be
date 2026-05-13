package com.example.movie_ticket_be.user.repository;

import com.example.movie_ticket_be.user.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role,String> {
}
