package com.example.movie_ticket_be.movie.dto.response;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GenreResponse {
    Long genreId;
    String name;
    String description;
    EntityStatus entityStatus;
}
