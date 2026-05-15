package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.movie.dto.request.PersonRequest;
import com.example.movie_ticket_be.movie.dto.response.PersonResponse;
import com.example.movie_ticket_be.movie.service.PersonService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/persons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class PersonController {
    private final PersonService personService;

    @PostMapping
    ApiResponse<PersonResponse> createPerson(@RequestBody PersonRequest personRequest) {
        return ApiResponse.<PersonResponse>builder()
                .result(personService.createPerson(personRequest))
                .message("Thêm diễn viên thành công")
                .build();
    }

    @PostMapping("/bulk")
    ApiResponse<List<PersonResponse>> createPersons(@RequestBody List<PersonRequest> requests) {
        return ApiResponse.<List<PersonResponse>>builder()
                .result(personService.createPersons(requests))
                .message("Đã thêm thành công " + requests.size())
                .build();
    }

    @GetMapping
    ApiResponse<List<PersonResponse>> getAllPersons() {
        return ApiResponse.<List<PersonResponse>>builder()
                .result(personService.getAllPersons())
                .build();
    }

}