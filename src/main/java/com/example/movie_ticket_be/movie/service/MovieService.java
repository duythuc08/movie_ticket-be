package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.request.MovieCreationRequest;
import com.example.movie_ticket_be.movie.dto.response.AdminMovieResponse;
import com.example.movie_ticket_be.movie.dto.response.MovieResponse;
import com.example.movie_ticket_be.movie.dto.response.PagedMovieResponse;
import com.example.movie_ticket_be.movie.entity.Genre;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.entity.Person;
import com.example.movie_ticket_be.movie.enums.MovieStatus;
import com.example.movie_ticket_be.movie.mapper.MovieMapper;
import com.example.movie_ticket_be.movie.repository.GenreRepository;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.movie.repository.PersonRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class MovieService {
    MovieRepository movieRepository;
    MovieMapper movieMapper;
    private final GenreRepository genreRepository;
    private final PersonRepository personRepository;
    private final CloudinaryService cloudinaryService;

    @PreAuthorize("hasRole('ADMIN')")
    public MovieResponse createMovie(MovieCreationRequest request){
        if(movieRepository.existsByTitle(request.getTitle())){
            throw new AppException(ErrorCode.MOVIE_EXISTED);
        }
        Movies movie = movieMapper.toMovies(request);

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

        movie.setCreatedAt(LocalDateTime.now());

        return movieMapper.toMovieResponse(movieRepository.save(movie));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public MovieResponse createMovie(MovieCreationRequest request, MultipartFile posterFile){
        if(movieRepository.existsByTitle(request.getTitle())){
            throw new AppException(ErrorCode.MOVIE_EXISTED);
        }
        Movies movie = movieMapper.toMovies(request);

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

        movie.setCreatedAt(LocalDateTime.now());

        if (posterFile != null && !posterFile.isEmpty()) {
            try {
                String url = cloudinaryService.uploadFile(posterFile);
                movie.setPosterUrl(url);
            } catch (IOException e) {
                log.error("Failed to upload movie poster", e);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        return movieMapper.toMovieResponse(movieRepository.save(movie));
    }
    @PreAuthorize("hasRole('ADMIN')")
    public List<AdminMovieResponse> getAdminMovies(){
        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toAdminMovieResponse)
                .toList();
    }
    public MovieResponse getMovieById(Long movieId) {
        Movies movie = movieRepository.findByMovieId(movieId)
                .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
        return movieMapper.toMovieResponse(movie);
    }
    public List<MovieResponse> getMovies(){
        return movieRepository.findAll()
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }

    public List<MovieResponse> getMoviesShowing(){
        return movieRepository.findByMovieStatus(MovieStatus.NOW_SHOWING)
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }
    public List<MovieResponse> getMoviesComingSoon(){
        return movieRepository.findByMovieStatus(MovieStatus.COMING_SOON)
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }
    public List<MovieResponse> getMoviesImax(){
        return movieRepository.findByMovieStatus(MovieStatus.IMAX)
                .stream()
                .map(movieMapper::toMovieResponse)
                .toList();
    }

    public List<MovieResponse> getMoviesShowingPaged(int page, int size) {
        return movieRepository.findByMovieStatus(
                MovieStatus.NOW_SHOWING,
                PageRequest.of(page, size, Sort.by("movieId").descending()))
                .getContent().stream().map(movieMapper::toMovieResponse).toList();
    }

    public List<MovieResponse> getMoviesComingSoonPaged(int page, int size) {
        return movieRepository.findByMovieStatus(
                MovieStatus.COMING_SOON,
                PageRequest.of(page, size, Sort.by("movieId").descending()))
                .getContent().stream().map(movieMapper::toMovieResponse).toList();
    }

    public List<MovieResponse> getMoviesImaxPaged(int page, int size) {
        return movieRepository.findByMovieStatus(
                MovieStatus.IMAX,
                PageRequest.of(page, size, Sort.by("movieId").descending()))
                .getContent().stream().map(movieMapper::toMovieResponse).toList();
    }


    private PagedMovieResponse buildPagedResponse(MovieStatus status, int page, int size) {
        Page<Movies> result = movieRepository.findByMovieStatus(
                status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        return PagedMovieResponse.builder()
                .content(result.getContent().stream().map(movieMapper::toMovieResponse).toList())
                .currentPage(page)
                .pageSize(size)
                .totalPages(result.getTotalPages())
                .totalElements(result.getTotalElements())
                .build();
    }

    public PagedMovieResponse getShowingMoviesPagedResponse(int page, int size) {
        return buildPagedResponse(MovieStatus.NOW_SHOWING, page, size);
    }

    public PagedMovieResponse getComingSoonMoviesPagedResponse(int page, int size) {
        return buildPagedResponse(MovieStatus.COMING_SOON, page, size);
    }

    public PagedMovieResponse getImaxMoviesPagedResponse(int page, int size) {
        return buildPagedResponse(MovieStatus.IMAX, page, size);
    }

}
