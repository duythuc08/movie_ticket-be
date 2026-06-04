package com.example.movie_ticket_be.user.dto.response;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserClientRespone {

    String userId;
    String username;
    String firstname;
    String lastname;
    String phoneNumber;
    LocalDate birthday;
    int loyaltyPoints;
    String memberShipTierName;
}
