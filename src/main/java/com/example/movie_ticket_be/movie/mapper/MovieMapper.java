package com.example.movie_ticket_be.movie.mapper;


import com.example.movie_ticket_be.movie.dto.request.MovieCreationRequest;
import com.example.movie_ticket_be.movie.dto.request.MovieUpdateRequest;
import com.example.movie_ticket_be.movie.dto.response.AdminMovieResponse;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.movie.entity.Movies;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    Movies toMovies(MovieCreationRequest request);

    @Mapping(target = "genre", ignore = true)
    @Mapping(target = "castPersons", ignore = true)
    @Mapping(target = "directors", ignore = true)
    void updateMovies(@MappingTarget Movies movie, MovieUpdateRequest request);

    MovieResponse toMovieResponse(Movies movies);

    AdminMovieResponse toAdminMovieResponse(Movies movies);

}
