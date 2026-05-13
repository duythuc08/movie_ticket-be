package com.example.movie_ticket_be.user.service;


import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.user.dto.response.MembershipTierResponse;
import com.example.movie_ticket_be.user.entity.MembershipTier;
import com.example.movie_ticket_be.user.mapper.MembershipTierMapper;
import com.example.movie_ticket_be.user.repository.MembershipTierRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class MembershipTierService {
    MembershipTierRepository membershipTierRepository;
    MembershipTierMapper membershipTierMapper;
    public MembershipTierResponse getMembershipTierByName(String name){
        MembershipTier tier = membershipTierRepository.findByName(name)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBERSHIP_TIER_NOT_FOUND));
        return membershipTierMapper.toMembershipTierResponse(tier);
    }
    public List<MembershipTierResponse> getMembershipTierList(){
        return membershipTierRepository.findAll().stream()
                .map(membershipTierMapper::toMembershipTierResponse)
                .toList();
    }
}
