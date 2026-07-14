package com.example.movie_ticket_be.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonInclude;
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
public class ResetPasswordRequest {
	@NotBlank
	@Size(min = 8, message = "Mật khẩu phải có ít nhất 8 ký tự")
	private String newPassword;
}
