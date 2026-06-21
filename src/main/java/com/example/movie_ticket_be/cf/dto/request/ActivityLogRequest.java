package com.example.movie_ticket_be.cf.dto.request;

import com.example.movie_ticket_be.cf.enums.ActionType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ActivityLogRequest {
    ActionType actionType;
    Long movieId;
    Map<String, Object> metadata;
}
