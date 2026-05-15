package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.request.GenreCreationRequest;
import com.example.movie_ticket_be.movie.dto.response.GenreResponse;
import com.example.movie_ticket_be.movie.entity.Genre;
import com.example.movie_ticket_be.movie.mapper.GenreMapper;
import com.example.movie_ticket_be.movie.repository.GenreRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class GenreService {
    GenreRepository genreRepository;
    GenreMapper genreMapper;


    public List<GenreResponse> getGenres()
    {
        return genreRepository.findAll()
                .stream()
                .map(genreMapper::toGenreRespone).toList();
    }

    @PreAuthorize("hasRole('ADMIN')")
    public GenreResponse createGenre(GenreCreationRequest request){
        if(genreRepository.existsByName(request.getName())){
            throw new AppException(ErrorCode.GENRE_EXISTED);
        }
        Genre genre = genreMapper.toGenre(request);
        return genreMapper.toGenreRespone(genreRepository.save(genre));
    }


}
