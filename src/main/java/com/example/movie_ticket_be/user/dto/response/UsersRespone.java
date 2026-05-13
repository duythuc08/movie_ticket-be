package com.example.movie_ticket_be.user.dto.response;

import com.example.movie_ticket_be.user.enums.UserStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsersRespone {

    String username; //su dung email
    String password;
    String firstname;
    String lastname;
    String phoneNumber;
    LocalDate birthday;

    @Enumerated(EnumType.STRING)
    UserStatus userStatus;

    Set<RoleResponse> roles;
    boolean enabled;
}
