package com.example.movie_ticket_be.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.SuperBuilder;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import lombok.experimental.FieldDefaults;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Permission extends BaseEntity {
	@Id
	String name;
	String description;
}
