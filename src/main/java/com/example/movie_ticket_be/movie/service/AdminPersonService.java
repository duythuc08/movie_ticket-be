package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.request.PersonRequest;
import com.example.movie_ticket_be.movie.dto.response.PersonResponse;
import com.example.movie_ticket_be.movie.entity.Person;
import com.example.movie_ticket_be.movie.mapper.PersonMapper;
import com.example.movie_ticket_be.movie.repository.PersonRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminPersonService {
    PersonRepository personRepository;
    PersonMapper personMapper;

    public PersonResponse createPerson(PersonRequest request) {
        if (personRepository.existsByNameAndMovieRole(request.getName(), request.getMovieRole())) {
            throw new AppException(ErrorCode.PERSON_EXISTED);
        }
        Person person = personMapper.toPerson(request);
        person.setEntityStatus(EntityStatus.ACTIVE);
        return personMapper.toPersonResponse(personRepository.save(person));
    }

    public List<PersonResponse> createPersons(List<PersonRequest> requests) {
        return requests.stream().map(this::createPerson).toList();
    }

    public List<PersonResponse> getAllPersons() {
        return personRepository.findAll().stream()
                .map(personMapper::toPersonResponse)
                .toList();
    }

    public void changeStatus(long id, EntityStatus entityStatus) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));
        person.setEntityStatus(entityStatus);
        personRepository.save(person);
    }
}
