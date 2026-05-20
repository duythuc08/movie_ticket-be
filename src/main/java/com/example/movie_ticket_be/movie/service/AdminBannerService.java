package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.request.BannerRequest;
import com.example.movie_ticket_be.movie.dto.response.BannerResponse;
import com.example.movie_ticket_be.movie.entity.Banner;
import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.movie.enums.BannerType;
import com.example.movie_ticket_be.movie.mapper.BannerMapper;
import com.example.movie_ticket_be.movie.repository.BannerRepository;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.promotion.entity.Event;
import com.example.movie_ticket_be.promotion.repository.EventRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminBannerService {
    BannerRepository bannerRepository;
    BannerMapper bannerMapper;
    MovieRepository movieRepository;
    EventRepository eventRepository;

    public BannerResponse createBanner(BannerRequest request) {
        if (bannerRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.BANNER_EXISTED);
        }
        validateBannerRequest(request);
        Banner banner = bannerMapper.toBanner(request);
        setRelations(banner, request);
        return bannerMapper.toBannerResponse(bannerRepository.save(banner));
    }

    public List<BannerResponse> createBanners(List<BannerRequest> requests) {
        return requests.stream().map(this::createBanner).toList();
    }

    private void validateBannerRequest(BannerRequest request) {
        if (request.getBannerType() == BannerType.MOVIE) {
            if (request.getMovieId() == null) throw new AppException(ErrorCode.MOVIE_ID_REQUIRED);
            if (!movieRepository.existsByMovieId(request.getMovieId())) throw new AppException(ErrorCode.MOVIE_NOT_FOUND);
            if (request.getEventId() != null) throw new AppException(ErrorCode.EVENT_ID_NOT_ALLOWED);
        } else if (request.getBannerType() == BannerType.EVENT) {
            if (request.getEventId() == null) throw new AppException(ErrorCode.EVENT_ID_REQUIRED);
            if (!eventRepository.existsByEventId(request.getEventId())) throw new AppException(ErrorCode.EVENT_NOT_FOUND);
            if (request.getMovieId() != null) throw new AppException(ErrorCode.MOVIE_ID_NOT_ALLOWED);
        } else {
            throw new AppException(ErrorCode.BANNER_TYPE_INVALID);
        }
    }

    private void setRelations(Banner banner, BannerRequest request) {
        if (request.getMovieId() != null) {
            Movies movie = movieRepository.findByMovieId(request.getMovieId())
                    .orElseThrow(() -> new AppException(ErrorCode.MOVIE_NOT_FOUND));
            banner.setMovies(movie);
        }
        if (request.getEventId() != null) {
            Event event = eventRepository.findByEventId(request.getEventId())
                    .orElseThrow(() -> new AppException(ErrorCode.EVENT_NOT_FOUND));
            banner.setEvent(event);
        }
    }
}
