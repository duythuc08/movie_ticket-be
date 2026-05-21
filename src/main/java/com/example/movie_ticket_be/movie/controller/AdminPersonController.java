package com.example.movie_ticket_be.movie.controller;

import com.example.movie_ticket_be.core.dto.ApiResponse;
import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.movie.dto.request.PersonRequest;
import com.example.movie_ticket_be.movie.dto.response.PersonResponse;
import com.example.movie_ticket_be.movie.entity.Genre;
import com.example.movie_ticket_be.movie.entity.Person;
import com.example.movie_ticket_be.movie.service.AdminPersonService;
import com.turkraft.springfilter.boot.Filter;

import io.swagger.v3.oas.annotations.Parameter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
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
    public ApiResponse<Page<PersonResponse>> getAllPersons(
            @Parameter(name = "filter", required = false) @Filter Specification<Person> spec,
            @ParameterObject @PageableDefault(sort = "createdAt", direction = Direction.DESC) Pageable pageable) {
        return ApiResponse.<Page<PersonResponse>>builder()
                .result(adminPersonService.getAllPersons(spec, pageable))
                .message("Lấy danh sách người thành công")
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<PersonResponse> getPersonById(@PathVariable long id) {
        return ApiResponse.<PersonResponse>builder()
                .result(adminPersonService.getPersonById(id))
                .message("Lấy thông tin chi tiết thành công")
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
