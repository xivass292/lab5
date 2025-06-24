package com.example.javalabaip.service;

import com.example.javalabaip.aspect.LoggingAspect;
import com.example.javalabaip.cache.CacheManager;
import com.example.javalabaip.dto.UserDto;
import com.example.javalabaip.exception.GlobalExceptionHandler;
import com.example.javalabaip.model.User;
import com.example.javalabaip.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final CacheManager cacheManager;
    private final LoggingAspect loggingAspect;

    public UserService(UserRepository userRepository, CacheManager cacheManager, LoggingAspect loggingAspect) {
        this.userRepository = userRepository;
        this.cacheManager = cacheManager;
        this.loggingAspect = loggingAspect;
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        String cacheKey = "findAll";
        if (cacheManager.containsUserListKey(cacheKey)) {
            return cacheManager.getUserList(cacheKey);
        }

        List<UserDto> result = userRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        cacheManager.putUserList(cacheKey, result);
        return result;
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        if (cacheManager.containsUserKey(id)) {
            return cacheManager.getUser(id);
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        UserDto result = convertToDto(user);
        cacheManager.putUser(id, result);
        return result;
    }

    @Transactional(readOnly = true)
    public UserDto findByUsername(String username) {
        String cacheKey = "findByUsername:" + username;
        if (cacheManager.containsUserListKey(cacheKey)) {
            return cacheManager.getUserList(cacheKey).get(0);
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found with username: " + username));
        UserDto result = convertToDto(user);
        cacheManager.putUserList(cacheKey, List.of(result));
        cacheManager.putUser(user.getId(), result); // Also cache by ID
        return result;
    }

    @Transactional
    public UserDto create(UserDto userDto) {
        User user = new User();
        user.setUsername(userDto.getUsername());
        User savedUser = userRepository.save(user);
        UserDto result = convertToDto(savedUser);
        cacheManager.clearAllCache();
        return result;
    }

    @Transactional
    public UserDto update(Long id, UserDto userDto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        String oldUsername = user.getUsername();
        user.setUsername(userDto.getUsername());
        User updatedUser = userRepository.save(user);
        UserDto result = convertToDto(updatedUser);
        cacheManager.clearAllCache();
        return result;
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
        cacheManager.clearAllCache();
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        return dto;
    }

    @Transactional
    public List<UserDto> createBulk(List<@Valid UserDto> userDtos) {

        if (userDtos == null || userDtos.isEmpty()) {
            return Collections.emptyList();
        }

        List<User> users = userDtos.stream().map(dto -> {
            if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Пользователь с именем " + dto.getUsername() + " уже существует");
            }
            User user = new User();
            user.setUsername(dto.getUsername());
            return user;
        }).collect(Collectors.toList());

        List<User> savedUsers = userRepository.saveAll(users);
        List<UserDto> result = savedUsers.stream().map(user -> {
            UserDto dto = new UserDto();
            dto.setId(user.getId());
            dto.setUsername(user.getUsername());
            return dto;
        }).collect(Collectors.toList());

        cacheManager.clearAllCache();
        return result;
    }
}
