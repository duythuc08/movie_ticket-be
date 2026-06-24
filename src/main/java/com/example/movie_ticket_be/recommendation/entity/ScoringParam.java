package com.example.movie_ticket_be.recommendation.entity;

import com.example.movie_ticket_be.recommendation.enums.ParamName;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "scoring_params")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScoringParam {
    @Id
    @Enumerated(EnumType.STRING)
    ParamName paramName;

    BigDecimal paramValue;
    LocalDateTime computeAt;
    Integer sampleSize; //Số lượng mẫu được sử dụng để tính median
}
