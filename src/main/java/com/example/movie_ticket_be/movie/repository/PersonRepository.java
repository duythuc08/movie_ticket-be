package com.example.movie_ticket_be.movie.repository;

import com.example.movie_ticket_be.movie.entity.Person;
import com.example.movie_ticket_be.movie.enums.MovieRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonRepository extends JpaRepository<Person, Long> {
    Optional<Person> findByName(String name);
    boolean existsByName(String name);
    boolean existsByNameAndMovieRole(String name, MovieRole movieRole);
}
