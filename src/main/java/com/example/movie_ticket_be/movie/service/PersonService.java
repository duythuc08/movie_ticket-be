package com.example.movie_ticket_be.movie.service;


import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.request.PersonRequest;
import com.example.movie_ticket_be.movie.dto.response.PersonResponse;
import com.example.movie_ticket_be.movie.entity.Person;
import com.example.movie_ticket_be.movie.mapper.PersonMapper;
import com.example.movie_ticket_be.movie.repository.PersonRepository;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@Service
@FieldDefaults(level = AccessLevel.PRIVATE,makeFinal = true)
public class PersonService {

    private final PersonRepository personRepository;
    private PersonMapper personMapper;
    private final CloudinaryService cloudinaryService;

    public PersonService(PersonRepository personRepository, PersonMapper personMapper, CloudinaryService cloudinaryService) {
        this.personRepository = personRepository;
        this.personMapper = personMapper;
        this.cloudinaryService = cloudinaryService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PersonResponse createPerson(PersonRequest request) {
        if (personRepository.existsByNameAndMovieRole((request.getName()), request.getMovieRole())) {
            throw new AppException(ErrorCode.PERSON_EXISTED);
        }
        Person person = personMapper.toPerson(request);
        return personMapper.toPersonResponse(personRepository.save(person));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public PersonResponse createPerson(PersonRequest request, MultipartFile avatarFile) {
        if (personRepository.existsByNameAndMovieRole((request.getName()), request.getMovieRole())) {
            throw new AppException(ErrorCode.PERSON_EXISTED);
        }
        Person person = personMapper.toPerson(request);

        if (avatarFile != null && !avatarFile.isEmpty()) {
            try {
                String url = cloudinaryService.uploadFile(avatarFile);
                person.setAvatarUrl(url);
            } catch (IOException e) {
                log.error("Failed to upload person avatar", e);
                throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
            }
        }

        return personMapper.toPersonResponse(personRepository.save(person));
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<PersonResponse> createPersons(List<PersonRequest> requests) {
        return requests.stream()
                .map(request -> {
                    if (personRepository.existsByNameAndMovieRole((request.getName()), request.getMovieRole())) {
                        throw new AppException(ErrorCode.PERSON_EXISTED);
                    }
                    Person person = personMapper.toPerson(request);
                    System.out.println("ROLE = " + person.getMovieRole());
                    return personMapper.toPersonResponse(personRepository.save(person));
                })
                .toList();
    }
    public List<PersonResponse> getAllPersons() {
        return personRepository.findAll()
                .stream()
                .map(personMapper:: toPersonResponse)
                .toList();
    }
}
