package com.example.movie_ticket_be.movie.mapper;


import com.example.movie_ticket_be.movie.dto.request.MovieCreationRequest;
import com.example.movie_ticket_be.movie.dto.response.AdminMovieResponse;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.movie.entity.Movies;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MovieMapper {
    Movies toMovies(MovieCreationRequest request);

    MovieResponse toMovieResponse(Movies movies);

    AdminMovieResponse toAdminMovieResponse(Movies movies);

}
