package com.example.movie_ticket_be.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ForgotPasswordRequest {
    @NotBlank(message = "USERNAME_REQUIRED")
    @Size(min = 3, message = "USERNAME_INVALID")
    @Email(message = "USERNAME_MUST_BE_EMAIL")
    String username;

}
