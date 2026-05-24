package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.response.BannerResponse;
import com.example.movie_ticket_be.movie.mapper.BannerMapper;
import com.example.movie_ticket_be.movie.repository.BannerRepository;
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
public class BannerService {
    BannerRepository bannerRepository;
    BannerMapper bannerMapper;

    public List<BannerResponse> getBannersActive() {
        return bannerRepository.findAllByOrderByPriorityAsc().stream()
                .map(bannerMapper::toBannerResponse)
                .toList();
    }

    public List<BannerResponse> getBannersByActive() {
        return bannerRepository.findByActiveTrueOrderByPriorityAsc().stream()
                .map(bannerMapper::toBannerResponse)
                .toList();
    }

    public BannerResponse getBannerByMovieId(Long movieId) {
        var banner = bannerRepository.findByMovies_MovieId(movieId);
        if (banner == null) throw new AppException(ErrorCode.BANNER_NOT_FOUND);
        return bannerMapper.toBannerResponse(banner);
    }
}
