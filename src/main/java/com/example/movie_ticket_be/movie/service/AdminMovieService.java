package com.example.movie_ticket_be.movie.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.request.MovieCreationRequest;
import com.example.movie_ticket_be.movie.dto.request.MovieUpdateRequest;
import com.example.movie_ticket_be.movie.dto.response.AdminMovieResponse;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.movie.entity.Genre;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.entity.Person;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.movie.mapper.MovieMapper;
import com.example.movie_ticket_be.movie.repository.GenreRepository;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.movie.repository.PersonRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class AdminMovieService {
    MovieRepository movieRepository;
    MovieMapper movieMapper;
    GenreRepository genreRepository;
    PersonRepository personRepository;
    
    @Transactional
    public MovieResponse createAdminMovie(MovieCreationRequest request) {
        if (movieRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.MOVIE_EXISTED);
        }
        
        Movies movie = movieMapper.toMovies(request);
        if (request.getReleaseDate().isBefore(LocalDateTime.now())){
            movie.setMovieStatus(MovieStatus.COMING_SOON);
        } else if (request.getReleaseDate().equals(LocalDateTime.now())){
            movie.setMovieStatus(MovieStatus.NOW_SHOWING);
        }


        Set<Genre> genres = request.getGenreName().stream()
                .map(name -> genreRepository.findByName(name)
                        .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND)))
                .collect(Collectors.toSet());
        movie.setGenre(genres);

        Set<Person> castPersons = request.getCastIds().stream()
                .map(id -> personRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND)))
                .collect(Collectors.toSet());
        movie.setCastPersons(castPersons);

        Set<Person> directors = request.getDirectorIds().stream()
                .map(id -> personRepository.findById(id)
                        .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND)))
                .collect(Collectors.toSet());
        movie.setDirectors(directors);
        return movieMapper.toMovieResponse(movieRepository.save(movie));
    }

    public Page<AdminMovieResponse> getAdminMovies(Specification<Movies> spec, Pageable pageable) {
        return movieRepository.findAll(spec, pageable)
                .map(movieMapper::toAdminMovieResponse);
    }

    public AdminMovieResponse getAdminMovieDetail(long id){
        Movies movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        return movieMapper.toAdminMovieResponse(movie);
    }

    @Transactional
    public AdminMovieResponse updateAdminMovie(long id, MovieUpdateRequest request){
        Movies movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        if (request.getReleaseDate() != null && request.getReleaseDate().isBefore(LocalDateTime.now())){
            throw new AppException(ErrorCode.MOVIE_RELEASE_DATE_NOT_VALID);
        }
        
        movieMapper.updateMovies(movie, request);

        if (request.getGenreName() != null) {
            Set<Genre> genres = request.getGenreName().stream()
                    .map(name -> genreRepository.findByName(name)
                            .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND)))
                    .collect(Collectors.toSet());
            movie.setGenre(genres);
        }

        if (request.getCastIds() != null) {
            Set<Person> castPersons = request.getCastIds().stream()
                    .map(personId -> personRepository.findById(personId)
                            .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND)))
                    .collect(Collectors.toSet());
            movie.setCastPersons(castPersons);
        }

        if (request.getDirectorIds() != null) {
            Set<Person> directors = request.getDirectorIds().stream()
                    .map(personId -> personRepository.findById(personId)
                            .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND)))
                    .collect(Collectors.toSet());
            movie.setDirectors(directors);
        }
        return movieMapper.toAdminMovieResponse(movieRepository.save(movie));
    }

    @Transactional
    public void changeStatus(long id, EntityStatus entityStatus) {
        Movies movie = movieRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        movie.setEntityStatus(entityStatus);
        movieRepository.save(movie);
    }
}
