package com.example.movie_ticket_be.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="membership_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipTier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long tierId;

    @Column(nullable = false, length = 50)
    String name;

    @Lob
    String description;

    Long pointsRequired;

    @Column(precision = 5, scale = 2)
    BigDecimal discountPercent;

    @Column(precision = 5, scale = 2)
    BigDecimal birthdayDiscount;


    @CreationTimestamp
    LocalDateTime createdAt;

    @UpdateTimestamp
    LocalDateTime updatedAt;
}
