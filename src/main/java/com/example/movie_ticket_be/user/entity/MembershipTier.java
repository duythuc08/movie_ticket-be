package com.example.movie_ticket_be.user.entity;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="membership_tiers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipTier extends BaseEntity {
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

}
