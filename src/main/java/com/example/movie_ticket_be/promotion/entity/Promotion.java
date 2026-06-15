package com.example.movie_ticket_be.promotion.entity;

import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.promotion.enums.PromotionStatus;
import com.example.movie_ticket_be.promotion.enums.PromotionType;
import com.example.movie_ticket_be.showtime.enums.DayOfWeek;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "promotion")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Promotion extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	Long promotionId;

	@Column(unique = true, nullable = false)
	String code;
	String name;
	@Lob
	String description;
	BigDecimal discountValue;
	BigDecimal minOrderValue;
	BigDecimal maxDiscountAmount;
	Integer useLimit;
	Integer usedCount;
	Boolean isPublic;

	LocalDateTime startTime;
	LocalDateTime endTime;

	@OneToMany(mappedBy = "promotion")
	Set<UserPromotion> userPromotion;

	@ManyToMany
	@JoinTable(name = "promotion_movies", joinColumns = @JoinColumn(name = "promotion_id"), inverseJoinColumns = @JoinColumn(name = "movie_id"))
	Set<Movies> applicableMovies;

	@ElementCollection(targetClass = DayOfWeek.class)
	@Enumerated(EnumType.STRING)
	Set<DayOfWeek> dayOfWeek;

	@Enumerated(EnumType.STRING)
	PromotionStatus status;

	@Enumerated(EnumType.STRING)
	PromotionType type;

	@PrePersist
	@Override
	protected void onCreate() {
		super.onCreate();
		if (this.status == null) {
			this.status = PromotionStatus.DRAFT;
		}
		if (this.isPublic == null) {
			this.isPublic = true;
		}
	}
}
