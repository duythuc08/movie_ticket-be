package com.example.movie_ticket_be.movie.mapper;


import com.example.movie_ticket_be.movie.dto.request.GenreCreationRequest;
import com.example.movie_ticket_be.movie.dto.response.GenreResponse;
import com.example.movie_ticket_be.movie.entity.Genre;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface GenreMapper {
    Genre toGenre(GenreCreationRequest request);

    GenreResponse toGenreRespone(Genre genre);
}
