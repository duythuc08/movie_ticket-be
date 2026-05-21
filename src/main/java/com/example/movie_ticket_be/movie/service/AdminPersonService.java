package com.example.movie_ticket_be.movie.service;

import com.example.movie_ticket_be.core.enums.EntityStatus;
import com.example.movie_ticket_be.core.exception.AppException;
import com.example.movie_ticket_be.core.exception.ErrorCode;
import com.example.movie_ticket_be.movie.dto.request.PersonRequest;
import com.example.movie_ticket_be.movie.dto.response.PersonResponse;
import com.example.movie_ticket_be.movie.entity.Person;
import com.example.movie_ticket_be.movie.mapper.PersonMapper;
import com.example.movie_ticket_be.movie.repository.PersonRepository;

import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminPersonService {
    PersonRepository personRepository;
    PersonMapper personMapper;

    @Transactional
    public PersonResponse createPerson(PersonRequest request) {
        if (personRepository.existsByNameAndMovieRole(request.getName(), request.getMovieRole())) {
            throw new AppException(ErrorCode.PERSON_EXISTED);
        }
        Person person = personMapper.toPerson(request);
        person.setEntityStatus(EntityStatus.ACTIVE);
        return personMapper.toPersonResponse(personRepository.save(person));
    }

    @Transactional
    public List<PersonResponse> createPersons(List<PersonRequest> requests) {
        List<Person> persons = requests.stream().map(request -> {
            if (personRepository.existsByNameAndMovieRole(request.getName(), request.getMovieRole())) {
                throw new AppException(ErrorCode.PERSON_EXISTED);
            }
            Person person = personMapper.toPerson(request);
            person.setEntityStatus(EntityStatus.ACTIVE);
            return person;
        }).toList();

        return personRepository.saveAll(persons).stream()
                .map(personMapper::toPersonResponse)
                .toList();
    }

    public Page<PersonResponse> getAllPersons(
            Specification<Person> spec,
            Pageable pageable) {
        return personRepository.findAll(spec, pageable)
                .map(personMapper::toPersonResponse);
    }

    public PersonResponse getPersonById(long id) {
        return personMapper.toPersonResponse(personRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND)));
    }

    @Transactional
    public void changeStatus(long id, EntityStatus entityStatus) {
        Person person = personRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PERSON_NOT_FOUND));
        person.setEntityStatus(entityStatus);
        personRepository.save(person);
    }
}
