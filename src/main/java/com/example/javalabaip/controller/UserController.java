package com.example.javalabaip.controller;

import com.example.javalabaip.cache.CacheManager;
import com.example.javalabaip.dto.UserDto;
import com.example.javalabaip.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Validated
public class UserController {

    private final UserService userService;
    private final CacheManager cacheManager;

    @Autowired
    public UserController(UserService userService, CacheManager cacheManager) {
        this.userService = userService;
        this.cacheManager = cacheManager;
    }

    @PostMapping("/users")
    public ResponseEntity<UserDto> create(@Valid @RequestBody UserDto userDto) {
        UserDto result = userService.create(userDto);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> findById(@PathVariable Long id) {
        UserDto user = userService.findById(id);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> findAll() {
        List<UserDto> users = userService.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/by-username")
    public ResponseEntity<UserDto> findByUsername(@RequestParam("username") String username) {
        UserDto user = userService.findByUsername(username);
        return user != null ? ResponseEntity.ok(user) : ResponseEntity.notFound().build();
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> update(@PathVariable Long id, @Valid @RequestBody UserDto userDto) {
        UserDto result = userService.update(id, userDto);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}