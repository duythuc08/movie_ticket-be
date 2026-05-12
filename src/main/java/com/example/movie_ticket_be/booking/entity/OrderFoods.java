package com.example.movie_ticket_be.booking.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "order_food")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderFoods {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long orderFoodId;

    Integer quantity;
    BigDecimal unitPrice;
    BigDecimal totalPrice;

    @ManyToOne
    @JoinColumn(name = "order_id")
    Orders orders;

    @ManyToOne
    @JoinColumn(name = "food_id")
    Foods foods;
}