package com.example.movie_ticket_be.movie.mapper;


import com.example.movie_ticket_be.movie.dto.request.BannerRequest;
import com.example.movie_ticket_be.movie.dto.response.BannerResponse;
import com.example.movie_ticket_be.movie.entity.Banner;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BannerMapper {
    Banner toBanner(BannerRequest request);
    BannerResponse toBannerResponse(Banner banner);
}
