package com.example.movie_ticket_be.booking.entity;

import com.example.movie_ticket_be.booking.enums.FoodStatus;
import com.example.movie_ticket_be.booking.enums.FoodType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "food")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Foods {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    Long foodId;

    String name;
    @Lob
    String description;
    BigDecimal price;
    String imageUrl;
    Boolean isCombo;
    Integer stockQuantity;

    @Enumerated(EnumType.STRING)
    FoodType foodType;

    @Enumerated(EnumType.STRING)
    FoodStatus foodStatus;
}