package com.example.movie_ticket_be.auth.entity;

import com.example.movie_ticket_be.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VerificationToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String verificationCode;
    LocalDateTime verificationCodeExpiresAt;
    LocalDateTime lastSentAt;
    @OneToOne
    Users user;
    private boolean invalidated = false;
}