package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.request.GenreCreationRequest;
import com.example.movie_ticket_be.movie.dto.response.GenreResponse;
import com.example.movie_ticket_be.movie.entity.Genre;
import com.example.movie_ticket_be.movie.mapper.GenreMapper;
import com.example.movie_ticket_be.movie.repository.GenreRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminGenreService {
    GenreRepository genreRepository;
    GenreMapper genreMapper;

    public Page<GenreResponse> getAllGenre(Specification<Genre> spec, Pageable pageable) {
        return genreRepository.findAll(spec, pageable)
                .map(genreMapper::toGenreRespone);
    }

    @Transactional
    public GenreResponse createGenre(GenreCreationRequest request) {
        if (genreRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.GENRE_EXISTED);
        }
        Genre genre = genreMapper.toGenre(request);
        genre.setEntityStatus(EntityStatus.ACTIVE);
        return genreMapper.toGenreRespone(genreRepository.save(genre));
    }
    
    public GenreResponse getGenreDetail(long id){
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));
        return genreMapper.toGenreRespone(genre);
    }

    @Transactional
    public void changeStatus(long id, EntityStatus entityStatus) {
        Genre genre = genreRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.GENRE_NOT_FOUND));
        genre.setEntityStatus(entityStatus);
        genreRepository.save(genre);
    }
}
