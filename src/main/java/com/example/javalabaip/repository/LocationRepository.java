package com.example.javalabaip.repository;

import com.example.javalabaip.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    List<Location> findByUserId(Long userId);

    @Query("SELECT l FROM Location l JOIN l.user u WHERE u.username = :username")
    List<Location> findByUsername(String username);
}