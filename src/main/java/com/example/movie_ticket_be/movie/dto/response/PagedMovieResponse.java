package com.example.movie_ticket_be.movie.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PagedMovieResponse {
    List<MovieResponse> content;
    int currentPage;
    int pageSize;
    int totalPages;
    long totalElements;
}
