package com.example.movie_ticket_be.cinema.dto.request;

import com.example.movie_ticket_be.cinema.enums.CinemaStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AdminCinemaUpdateRequest {
	@NotBlank
	String name;
	@NotBlank
	String address;
	@Pattern(regexp = "^(0[3|5|7|8|9])[0-9]{8}$", message = "Số điện thoại không đúng định dạng Việt Nam")
	String phoneNumber;
	@Email
	String email;
	CinemaStatus cinemaStatus;
	List<AdminRoomRequest> rooms;
}
