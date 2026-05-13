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
import com.example.movie_ticket_be.promotion.repository.EventRepository;
import com.example.movie_ticket_be.movie.repository.MovieRepository;
import com.example.movie_ticket_be.promotion.entity.Event;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class BannerService {
    BannerRepository bannerRepository;
    BannerMapper bannerMapper;

    final MovieRepository movieRepository;
    final EventRepository eventRepository;
    final CloudinaryService cloudinaryService;

    @PreAuthorize("hasRole('ADMIN')")
    public BannerResponse createBanner(BannerRequest request) {
        if (bannerRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.BANNER_EXISTED);
        }

        if (request.getBannerType() == BannerType.MOVIE) {
            if (request.getMovieId() == null) {
                throw new AppException(ErrorCode.MOVIE_ID_REQUIRED);
            }
            if (!movieRepository.existsByMovieId(request.getMovieId())) {
                throw new AppException(ErrorCode.MOVIE_NOT_FOUND);
            }
            if (request.getEventId() != null) {
                throw new AppException(ErrorCode.EVENT_ID_NOT_ALLOWED);
            }
        } else if (request.getBannerType() == BannerType.EVENT) {
            if (request.getEventId() == null) {
                throw new AppException(ErrorCode.EVENT_ID_REQUIRED);
            }
            if (!eventRepository.existsByEventId(request.getEventId())) {
                throw new AppException(ErrorCode.EVENT_NOT_FOUND);
            }
            if (request.getMovieId() != null) {
                throw new AppException(ErrorCode.MOVIE_ID_NOT_ALLOWED);
            }
        } else {
            throw new AppException(ErrorCode.BANNER_TYPE_INVALID);
        }


        Banner banner = bannerMapper.toBanner(request);
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

        return bannerMapper.toBannerResponse(bannerRepository.save(banner));

    }

    @PreAuthorize("hasRole('ADMIN')")
    public BannerResponse createBanner(BannerRequest request, MultipartFile imageFile) {
        if (bannerRepository.existsByTitle(request.getTitle())) {
            throw new AppException(ErrorCode.BANNER_EXISTED);
        }

        if (request.getBannerType() == BannerType.MOVIE) {
            if (request.getMovieId() == null) {
                throw new AppException(ErrorCode.MOVIE_ID_REQUIRED);
            }
            if (!movieRepository.existsByMovieId(request.getMovieId())) {
                throw new AppException(ErrorCode.MOVIE_NOT_FOUND);
            }
            if (request.getEventId() != null) {
                throw new AppException(ErrorCode.EVENT_ID_NOT_ALLOWED);
            }
        } else if (request.getBannerType() == BannerType.EVENT) {
            if (request.getEventId() == null) {
                throw new AppException(ErrorCode.EVENT_ID_REQUIRED);
            }
            if (!eventRepository.existsByEventId(request.getEventId())) {
                throw new AppException(ErrorCode.EVENT_NOT_FOUND);
            }
            if (request.getMovieId() != null) {
                throw new AppException(ErrorCode.MOVIE_ID_NOT_ALLOWED);
            }
        } else {
            throw new AppException(ErrorCode.BANNER_TYPE_INVALID);
        }


        Banner banner = bannerMapper.toBanner(request);
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

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                String url = cloudinaryService.uploadFile(imageFile);
                banner.setImageUrl(url);
            } catch (IOException e) {
                log.error("Failed to upload banner image", e);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        return bannerMapper.toBannerResponse(bannerRepository.save(banner));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<BannerResponse> createBanners(List<BannerRequest> requests) {
        return requests.stream()
                .map(request -> {

                    if (bannerRepository.existsByTitle(request.getTitle())) {
                        throw new AppException(ErrorCode.BANNER_EXISTED);
                    }

                    if (request.getBannerType() == BannerType.MOVIE) {
                        if (request.getMovieId() == null) {
                            throw new AppException(ErrorCode.MOVIE_ID_REQUIRED);
                        }
                        if (!movieRepository.existsByMovieId(request.getMovieId())) {
                            throw new AppException(ErrorCode.MOVIE_NOT_FOUND);
                        }
                        if (request.getEventId() != null) {
                            throw new AppException(ErrorCode.EVENT_ID_NOT_ALLOWED);
                        }
                    } else if (request.getBannerType() == BannerType.EVENT) {
                        if (request.getEventId() == null) {
                            throw new AppException(ErrorCode.EVENT_ID_REQUIRED);
                        }
                        if (!eventRepository.existsByEventId(request.getEventId())) {
                            throw new AppException(ErrorCode.EVENT_NOT_FOUND);
                        }
                        if (request.getMovieId() != null) {
                            throw new AppException(ErrorCode.MOVIE_ID_NOT_ALLOWED);
                        }
                    } else {
                        throw new AppException(ErrorCode.BANNER_TYPE_INVALID);
                    }
                    Banner banner = bannerMapper.toBanner(request);
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

                    return bannerMapper.toBannerResponse(bannerRepository.save(banner));
                })
                .toList();
    }

    public List<BannerResponse> getBanners(){
        return bannerRepository.findAllByOrderByPriorityAsc()
                .stream()
                .map(bannerMapper :: toBannerResponse)
                .toList();
    }

    public List<BannerResponse> getBannersByActive(){
        return bannerRepository.findByActiveTrueOrderByPriorityAsc()
                .stream()
                .map(bannerMapper :: toBannerResponse)
                .toList();
    }

    public BannerResponse getBannerByMovieId(Long movieId){
        Banner banner = bannerRepository.findByMovies_MovieId(movieId);
        if(banner == null){
            throw new AppException(ErrorCode.BANNER_NOT_FOUND);
        }
        return bannerMapper.toBannerResponse(banner);
    }
}
