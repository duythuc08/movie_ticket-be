package com.example.movie_ticket_be.showtime.dto.request;

import com.example.movie_ticket_be.showtime.enums.ShowTimeStatus;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShowTimeRequest {
    LocalDateTime startTime;
    LocalDateTime endTime;

    Long movieId;
    Long roomId;

    @Enumerated(EnumType.STRING)
    ShowTimeStatus showTimeStatus;
}
