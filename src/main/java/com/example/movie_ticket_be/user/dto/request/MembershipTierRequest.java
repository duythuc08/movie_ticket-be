package com.example.movie_ticket_be.user.dto.request;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MembershipTierRequest {
	String name;
	String description;
	Long pointsRequired;
	BigDecimal discountPercent;
	BigDecimal birthdayDiscount;
}
