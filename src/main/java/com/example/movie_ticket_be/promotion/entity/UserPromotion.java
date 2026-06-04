package com.example.movie_ticket_be.promotion.entity;

import com.example.movie_ticket_be.booking.entity.Orders;
import com.example.movie_ticket_be.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPromotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    Users users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "promotion_id", nullable = false)
    Promotion promotion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    Orders order;

    boolean isUsed;
    LocalDateTime claimedAt;
    LocalDateTime usedAt;

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.claimedAt == null) {
            this.claimedAt = LocalDateTime.now();
        }
    }
}
