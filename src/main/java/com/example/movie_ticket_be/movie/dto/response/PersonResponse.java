
package com.example.movie_ticket_be.movie.dto.response;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.movie.enums.MovieRole;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PersonResponse {
	private Long id;
	private String name;
	private String avatarUrl;
	private List<MovieRole> movieRole;
	private EntityStatus entityStatus;
}
