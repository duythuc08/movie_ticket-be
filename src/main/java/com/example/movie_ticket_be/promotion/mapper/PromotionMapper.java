package com.example.movie_ticket_be.promotion.mapper;

import com.example.movie_ticket_be.movie.entity.Movies;
import com.example.movie_ticket_be.promotion.dto.request.PromotionRequest;
import com.example.movie_ticket_be.promotion.dto.response.AdminPromotionResponse;
import com.example.movie_ticket_be.promotion.entity.Promotion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface PromotionMapper {

	@Mapping(target = "promotionId", ignore = true)
	@Mapping(target = "applicableMovies", ignore = true)
	@Mapping(target = "userPromotion", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "usedCount", ignore = true)
	@Mapping(target = "entityStatus", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	Promotion toPromotion(PromotionRequest request);

	@Mapping(target = "applicableMovieIds", source = "applicableMovies", qualifiedByName = "moviesToIds")
	AdminPromotionResponse toAdminResponse(Promotion promotion);

	@Mapping(target = "promotionId", ignore = true)
	@Mapping(target = "applicableMovies", ignore = true)
	@Mapping(target = "userPromotion", ignore = true)
	@Mapping(target = "status", ignore = true)
	@Mapping(target = "usedCount", ignore = true)
	@Mapping(target = "entityStatus", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	void updatePromotion(@MappingTarget Promotion promotion, PromotionRequest request);

	@Named("moviesToIds")
	default Set<Long> moviesToIds(Set<Movies> movies) {
		if (movies == null)
			return Collections.emptySet();
		return movies.stream().map(Movies::getMovieId).collect(Collectors.toSet());
	}
}
