package com.example.movie_ticket_be.auth.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationResponse {
    String token; //tra token ve cho user
    boolean authenticated; //true khi username+password dung
    boolean enabled;
}
