package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.movie.dto.request.PersonRequest;
import com.example.movie_ticket_be.movie.dto.response.PersonResponse;
import com.example.movie_ticket_be.movie.service.AdminPersonService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/persons")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminPersonController {
    AdminPersonService adminPersonService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PersonResponse> createPerson(@RequestBody PersonRequest request) {
        return ApiResponse.<PersonResponse>builder()
                .result(adminPersonService.createPerson(request))
                .message("Thêm người thành công")
                .build();
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PersonResponse>> createPersons(@RequestBody List<PersonRequest> requests) {
        return ApiResponse.<List<PersonResponse>>builder()
                .result(adminPersonService.createPersons(requests))
                .message("Đã thêm thành công " + requests.size())
                .build();
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<PersonResponse>> getAllPersons() {
        return ApiResponse.<List<PersonResponse>>builder()
                .result(adminPersonService.getAllPersons())
                .build();
    }

    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> activate(@PathVariable long id) {
        adminPersonService.changeStatus(id, EntityStatus.ACTIVE);
        return ApiResponse.<Void>builder().message("Kích hoạt thành công").build();
    }

    @PutMapping("/{id}/inactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> inactivate(@PathVariable long id) {
        adminPersonService.changeStatus(id, EntityStatus.INACTIVE);
        return ApiResponse.<Void>builder().message("Vô hiệu hóa thành công").build();
    }
}
