package com.example.movie_ticket_be.booking.entity;

import com.example.movie_ticket_be.booking.enums.FoodStatus;
import com.example.movie_ticket_be.booking.enums.FoodType;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Entity
@Table(name = "food")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Foods extends BaseEntity {
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

    @PrePersist
    @Override
    protected void onCreate() {
        super.onCreate();
        if (this.foodStatus == null) {
            this.foodStatus = FoodStatus.IN_STOCK;
        }
    }
}