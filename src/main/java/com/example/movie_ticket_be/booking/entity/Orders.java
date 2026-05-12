package com.example.movie_ticket_be.booking.entity;

import com.example.movie_ticket_be.booking.enums.OrderStatus;
import com.example.movie_ticket_be.user.entity.Users;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Orders {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long orderId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    Users users;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<OrderTickets> orderTickets;

    @OneToMany(mappedBy = "orders", cascade = CascadeType.ALL, orphanRemoval = true)
    Set<OrderFoods> orderFoods;

    BigDecimal totalTicketPrice;
    BigDecimal totalFoodPrice;
    BigDecimal discountAmount;
    BigDecimal finalPrice;
    String promotionCode;

    @Enumerated(EnumType.STRING)
    OrderStatus orderStatus;

    String qrCode;

    LocalDateTime bookingTime;
    LocalDateTime expiredTime;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;

    int pointsEarned;
    BigDecimal memberDiscountAmount;
}