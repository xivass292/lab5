
package com.example.javalabaip.service;

import com.example.javalabaip.cache.CacheManager;
import com.example.javalabaip.dto.UserDto;
import com.example.javalabaip.model.User;
import com.example.javalabaip.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private User user;

    @Mock
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("testuser");
        when(userDto.getId()).thenReturn(1L);
        when(userDto.getUsername()).thenReturn("testuser");
    }

    @Test
    @DisplayName("Должен возвращать всех пользователей из кэша, если кэш содержит данные")
    void shouldReturnAllUsersFromCache() {
        String cacheKey = "findAll";
        List<UserDto> cachedUsers = List.of(userDto);
        when(cacheManager.containsUserListKey(cacheKey)).thenReturn(true);
        when(cacheManager.getUserList(cacheKey)).thenReturn(cachedUsers);

        List<UserDto> result = userService.findAll();

        assertEquals(cachedUsers, result);
        verify(userRepository, never()).findAll();
    }

    @Test
    @DisplayName("Должен возвращать всех пользователей из репозитория, если кэш пуст")
    void shouldReturnAllUsersFromRepository() {
        String cacheKey = "findAll";
        when(cacheManager.containsUserListKey(cacheKey)).thenReturn(false);
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.findAll();

        assertEquals(1, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        verify(cacheManager).putUserList(cacheKey, result);
    }

    @Test
    @DisplayName("Должен возвращать пустой список, если репозиторий пуст")
    void shouldReturnEmptyListWhenRepositoryEmpty() {
        String cacheKey = "findAll";
        when(cacheManager.containsUserListKey(cacheKey)).thenReturn(false);
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.findAll();

        assertTrue(result.isEmpty());
        verify(cacheManager).putUserList(cacheKey, result);
    }

    @Test
    @DisplayName("Должен возвращать пользователя по ID из кэша, если кэш содержит данные")
    void shouldReturnUserByIdFromCache() {
        when(cacheManager.containsUserKey(1L)).thenReturn(true);
        when(cacheManager.getUser(1L)).thenReturn(userDto);

        UserDto result = userService.findById(1L);

        assertEquals(userDto, result);
        verify(userRepository, never()).findByIdWithLocations(any());
    }

    @Test
    @DisplayName("Должен возвращать пользователя по ID из репозитория, если кэш пуст")
    void shouldReturnUserByIdFromRepository() {
        when(cacheManager.containsUserKey(1L)).thenReturn(false);
        when(userRepository.findByIdWithLocations(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.findById(1L);

        assertEquals("testuser", result.getUsername());
        verify(cacheManager).putUser(1L, result);
    }

    @Test
    @DisplayName("Должен бросать исключение, если пользователь по ID не найден")
    void shouldThrowExceptionWhenUserNotFoundById() {
        when(cacheManager.containsUserKey(1L)).thenReturn(false);
        when(userRepository.findByIdWithLocations(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findById(1L));
    }

    @Test
    @DisplayName("Должен возвращать пользователя по имени из кэша, если кэш содержит данные")
    void shouldReturnUserByUsernameFromCache() {
        String cacheKey = "findByUsername:testuser";
        when(cacheManager.containsUserListKey(cacheKey)).thenReturn(true);
        when(cacheManager.getUserList(cacheKey)).thenReturn(List.of(userDto));

        UserDto result = userService.findByUsername("testuser");

        assertEquals(userDto, result);
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("Должен возвращать пользователя по имени из репозитория, если кэш пуст")
    void shouldReturnUserByUsernameFromRepository() {
        String cacheKey = "findByUsername:testuser";
        when(cacheManager.containsUserListKey(cacheKey)).thenReturn(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        UserDto result = userService.findByUsername("testuser");

        assertEquals("testuser", result.getUsername());
        verify(cacheManager).putUserList(cacheKey, List.of(result));
        verify(cacheManager).putUser(1L, result);
    }

    @Test
    @DisplayName("Должен бросать исключение, если пользователь по имени не найден")
    void shouldThrowExceptionWhenUserNotFoundByUsername() {
        String cacheKey = "findByUsername:testuser";
        when(cacheManager.containsUserListKey(cacheKey)).thenReturn(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.findByUsername("testuser"));
    }

    @Test
    @DisplayName("Должен успешно создавать пользователя")
    void shouldCreateUserSuccessfully() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.create(userDto);

        assertEquals("testuser", result.getUsername());
        verify(cacheManager).clearAllCache();
    }

    @Test
    @DisplayName("Должен успешно обновлять пользователя")
    void shouldUpdateUserSuccessfully() {
        User updatedUser = mock(User.class);
        when(updatedUser.getId()).thenReturn(1L);
        when(updatedUser.getUsername()).thenReturn("updateduser");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        UserDto updatedDto = mock(UserDto.class);
        when(updatedDto.getId()).thenReturn(1L);
        when(updatedDto.getUsername()).thenReturn("updateduser");

        UserDto result = userService.update(1L, updatedDto);

        assertEquals("updateduser", result.getUsername());
        verify(cacheManager).clearAllCache();
    }

    @Test
    @DisplayName("Должен бросать исключение при обновлении несуществующего пользователя")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.update(1L, userDto));
    }

    @Test
    @DisplayName("Должен успешно удалять пользователя")
    void shouldDeleteUserSuccessfully() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
        verify(cacheManager).clearAllCache();
    }

    @Test
    @DisplayName("Должен бросать исключение при удалении несуществующего пользователя")
    void shouldThrowExceptionWhenDeletingNonExistentUser() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.delete(1L));
    }

    @Test
    @DisplayName("Должен успешно создавать несколько пользователей")
    void shouldCreateMultipleUsersSuccessfully() {
        UserDto userDto2 = mock(UserDto.class);
        User user2 = mock(User.class);
        when(userDto2.getUsername()).thenReturn("testuser2");
        when(user2.getId()).thenReturn(2L);
        when(user2.getUsername()).thenReturn("testuser2");

        List<UserDto> userDtos = List.of(userDto, userDto2);
        List<User> users = List.of(user, user2);

        when(userRepository.saveAll(any())).thenReturn(users);

        List<UserDto> result = userService.createBulk(userDtos);

        assertEquals(2, result.size());
        assertEquals("testuser", result.get(0).getUsername());
        assertEquals("testuser2", result.get(1).getUsername());
        verify(cacheManager).clearAllCache();
    }

    @Test
    @DisplayName("Должен возвращать пустой список при массовом создании, если входной список пуст")
    void shouldReturnEmptyListWhenCreateBulkWithEmptyList() {
        List<UserDto> userDtos = Collections.emptyList();

        List<UserDto> result = userService.createBulk(userDtos);

        assertTrue(result.isEmpty());
        verify(userRepository, never()).saveAll(any());
        verify(cacheManager, never()).clearAllCache();
    }
}
