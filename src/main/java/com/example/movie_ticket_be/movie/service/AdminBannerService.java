package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
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

	@Transactional
	public BannerResponse createBanner(BannerRequest request) {
		if (bannerRepository.existsByTitle(request.getTitle())) {
			throw new AppException(ErrorCode.BANNER_EXISTED);
		}
		validateBannerRequest(request);
		Banner banner = bannerMapper.toBanner(request);
		if (banner.getActive() == true) {
			banner.setEntityStatus(EntityStatus.ACTIVE);
		} else {
			banner.setEntityStatus(EntityStatus.INACTIVE);
		}
		setRelations(banner, request);
		return bannerMapper.toBannerResponse(bannerRepository.save(banner));
	}

	@Transactional
	public List<BannerResponse> createBanners(List<BannerRequest> requests) {
		List<Banner> banners = requests.stream().map(request -> {
			if (bannerRepository.existsByTitle(request.getTitle())) {
				throw new AppException(ErrorCode.BANNER_EXISTED);
			}
			validateBannerRequest(request);
			Banner banner = bannerMapper.toBanner(request);
			setRelations(banner, request);
			return banner;
		}).toList();

		return bannerRepository.saveAll(banners).stream().map(bannerMapper::toBannerResponse).toList();
	}

	@Transactional
	public BannerResponse updateBanner(Long id, BannerRequest request) {
		Banner banner = bannerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BANNER_NOT_FOUND));
		if (!banner.getTitle().equals(request.getTitle())) {
			if (bannerRepository.existsByTitle(request.getTitle())) {
				throw new AppException(ErrorCode.BANNER_EXISTED);
			}
		}
		validateBannerRequest(request);
		setRelations(banner, request);
		return bannerMapper.toBannerResponse(bannerRepository.save(banner));
	}

	@Transactional
	public void deleteBanner(Long id) {
		bannerRepository.deleteById(id);
	}

	public Page<BannerResponse> getAllBanners(Specification<Banner> spec, Pageable pageable) {
		return bannerRepository.findAll(spec, pageable).map(bannerMapper::toBannerResponse);
	}

	public BannerResponse getBannerById(Long id) {
		return bannerMapper.toBannerResponse(
				bannerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BANNER_NOT_FOUND)));
	}

	private void validateBannerRequest(BannerRequest request) {
		if (request.getBannerType() == BannerType.MOVIE) {
			if (request.getMovieId() == null)
				throw new AppException(ErrorCode.MOVIE_ID_REQUIRED);
			if (!movieRepository.existsByMovieId(request.getMovieId()))
				throw new AppException(ErrorCode.MOVIE_NOT_FOUND);
			if (request.getEventId() != null)
				throw new AppException(ErrorCode.EVENT_ID_NOT_ALLOWED);
		} else if (request.getBannerType() == BannerType.EVENT) {
			if (request.getEventId() == null)
				throw new AppException(ErrorCode.EVENT_ID_REQUIRED);
			if (!eventRepository.existsByEventId(request.getEventId()))
				throw new AppException(ErrorCode.EVENT_NOT_FOUND);
			if (request.getMovieId() != null)
				throw new AppException(ErrorCode.MOVIE_ID_NOT_ALLOWED);
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

	@Transactional
	public void changeStatus(long id, EntityStatus entityStatus) {
		Banner banner = bannerRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.BANNER_NOT_FOUND));
		banner.setEntityStatus(entityStatus);
		if (entityStatus == EntityStatus.ACTIVE) {
			banner.setActive(true);
		} else {
			banner.setActive(false);
		}
		bannerRepository.save(banner);
	}
}
