package com.example.movie_ticket_be.movie.entity;

import com.example.movie_ticket_be.movie.enums.MovieRole;
import com.example.movie_ticket_be.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import lombok.experimental.FieldDefaults;

import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "persons")
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Person extends BaseEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;
	private String avatarUrl;

	@Builder.Default
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "person_roles", joinColumns = @JoinColumn(name = "person_id"))
	@Enumerated(EnumType.STRING)
	@Column(name = "movie_role")
	private List<MovieRole> movieRole = new ArrayList<>();

}
