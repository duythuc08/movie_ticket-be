package com.example.movie_ticket_be.user.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.user.entity.MembershipTier;
import com.example.movie_ticket_be.user.repository.MembershipTierRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminMembershipTierService {
    MembershipTierRepository membershipTierRepository;

    public void changeStatus(long id, EntityStatus entityStatus) {
        MembershipTier tier = membershipTierRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MEMBERSHIP_TIER_NOT_FOUND));
        tier.setEntityStatus(entityStatus);
        membershipTierRepository.save(tier);
    }
}
