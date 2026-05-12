package com.example.movie_ticket_be.user.entity;

import com.example.movie_ticket_be.user.enums.UserStatus;
import com.example.movie_ticket_be.promotion.entity.UserPromotion;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Users {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String userId;

    @Column(unique = true)
    String username;
    String password;
    String firstname;
    String lastname;
    String phoneNumber;
    LocalDate birthday;

    @OneToMany(mappedBy = "users")
    Set<UserPromotion> userPromotions;

    @ManyToMany
    Set<Role> role;

    @Enumerated(EnumType.STRING)
    UserStatus userStatus;

    boolean enabled = false;
    LocalDateTime createdAt = LocalDateTime.now();

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @Column(name = "loyalty_points")
    int loyaltyPoints = 0;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tier_id")
    MembershipTier membershipTier;
}
