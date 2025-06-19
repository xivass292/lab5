package com.example.javalabaip.repository;

import com.example.javalabaip.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    @EntityGraph(attributePaths = "locations")
    Optional<User> findById(Long id);
}