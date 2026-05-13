package com.example.movie_ticket_be.user.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserClientRespone {

    String userId;
    String username; //su dung email
    String password;
    String firstname;
    String lastname;
    String phoneNumber;
    LocalDate birthday;
    int loyaltyPoints;
    String memberShipTierName;
}
