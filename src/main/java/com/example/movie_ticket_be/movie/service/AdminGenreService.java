package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
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
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminGenreService {
    GenreRepository genreRepository;
    GenreMapper genreMapper;

    public GenreResponse createGenre(GenreCreationRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.GENRE_EXISTED);
        }
        Genre genre = genreMapper.toGenre(request);
        genre.setEntityStatus(EntityStatus.ACTIVE);
        return genreMapper.toGenreRespone(genreRepository.save(genre));
    }

    public void changeStatus(long id, EntityStatus entityStatus) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));
        genre.setEntityStatus(entityStatus);
        genreRepository.save(genre);
    }
}
