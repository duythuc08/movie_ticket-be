package com.example.movie_ticket_be.auth.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.Date;

@Entity
@Table(name = "refresh_token")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RefreshToken {
	@Id
	String token;

	@Column(nullable = false)
	String userId;

	@Column(nullable = false)
	Date expiryTime;
}
