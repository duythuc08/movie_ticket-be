package com.example.movie_ticket_be.user.mapper;

import com.example.movie_ticket_be.user.dto.response.MembershipTierResponse;
import com.example.movie_ticket_be.user.entity.MembershipTier;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface MembershipTierMapper {
    MembershipTierResponse toMembershipTierResponse(MembershipTier membershipTier);
}
