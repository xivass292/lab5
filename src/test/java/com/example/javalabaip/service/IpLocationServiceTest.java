package com.example.javalabaip.service;

import com.example.javalabaip.cache.CacheManager;
import com.example.javalabaip.dto.LocationResponseDto;
import com.example.javalabaip.dto.UserDto;
import com.example.javalabaip.model.Location;
import com.example.javalabaip.model.User;
import com.example.javalabaip.repository.LocationRepository;
import com.example.javalabaip.repository.UserRepository;
import com.example.javalabaip.util.IpAddressValidator;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IpLocationServiceTest {

    @InjectMocks
    private IpLocationService ipLocationService;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CacheManager cacheManager;

    @Mock
    private IpAddressValidator ipAddressValidator;

    @Mock
    private Location location;

    @Mock
    private User user;

    @Mock
    private UserDto userDto;

    @Mock
    private LocationResponseDto locationDto;

    @BeforeEach
    void setUp() {
        when(user.getId()).thenReturn(1L);
        when(user.getUsername()).thenReturn("testuser");
        when(userDto.getUsername()).thenReturn("testuser");
        when(location.getId()).thenReturn(1L);
        when(location.getIpAddress()).thenReturn("192.168.1.1");
        when(location.getCity()).thenReturn("City");
        when(location.getCountry()).thenReturn("Country");
        when(location.getUser()).thenReturn(user);
        when(locationDto.getId()).thenReturn(1L);
        when(locationDto.getIpAddress()).thenReturn("192.168.1.1");
        when(locationDto.getCity()).thenReturn("City");
        when(locationDto.getCountry()).thenReturn("Country");
    }

    @Test
    @DisplayName("Должен возвращать все локации из кэша, если кэш содержит данные")
    void shouldReturnAllLocationsFromCache() {
        String cacheKey = "findAll";
        List<LocationResponseDto> cachedLocations = List.of(locationDto);
        when(cacheManager.containsLocationListKey(cacheKey)).thenReturn(true);
        when(cacheManager.getLocationList(cacheKey)).thenReturn(cachedLocations);

        List<LocationResponseDto> result = ipLocationService.findAll();

        assertEquals(cachedLocations, result);
        verify(locationRepository, never()).findAll();
    }

    @Test
    @DisplayName("Должен возвращать все локации из репозитория, если кэш пуст")
    void shouldReturnAllLocationsFromRepository() {
        String cacheKey = "findAll";
        when(cacheManager.containsLocationListKey(cacheKey)).thenReturn(false);
        when(locationRepository.findAll()).thenReturn(List.of(location));

        List<LocationResponseDto> result = ipLocationService.findAll();

        assertEquals(1, result.size());
        assertEquals("192.168.1.1", result.get(0).getIpAddress());
        verify(cacheManager).putLocationList(cacheKey, result);
    }

    @Test
    @DisplayName("Должен возвращать пустой список, если репозиторий пуст")
    void shouldReturnEmptyListWhenRepositoryEmpty() {
        String cacheKey = "findAll";
        when(cacheManager.containsLocationListKey(cacheKey)).thenReturn(false);
        when(locationRepository.findAll()).thenReturn(Collections.emptyList());

        List<LocationResponseDto> result = ipLocationService.findAll();

        assertTrue(result.isEmpty());
        verify(cacheManager).putLocationList(cacheKey, result);
    }

    @Test
    @DisplayName("Должен возвращать локацию по ID из кэша, если кэш содержит данные")
    void shouldReturnLocationByIdFromCache() {
        when(cacheManager.containsLocationKey(1L)).thenReturn(true);
        when(cacheManager.getLocation(1L)).thenReturn(locationDto);

        LocationResponseDto result = ipLocationService.findById(1L);

        assertEquals(locationDto, result);
        verify(locationRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Должен возвращать локацию по ID из репозитория, если кэш пуст")
    void shouldReturnLocationByIdFromRepository() {
        when(cacheManager.containsLocationKey(1L)).thenReturn(false);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        LocationResponseDto result = ipLocationService.findById(1L);

        assertEquals("192.168.1.1", result.getIpAddress());
        verify(cacheManager).putLocation(1L, result);
    }

    @Test
    @DisplayName("Должен бросать исключение, если локация по ID не найдена")
    void shouldThrowExceptionWhenLocationNotFoundById() {
        when(cacheManager.containsLocationKey(1L)).thenReturn(false);
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ipLocationService.findById(1L));
    }

    @Test
    @DisplayName("Должен возвращать локации по имени пользователя из кэша, если кэш содержит данные")
    void shouldReturnLocationsByUsernameFromCache() {
        String cacheKey = "findByUsername:testuser";
        List<LocationResponseDto> cachedLocations = List.of(locationDto);
        when(cacheManager.containsLocationListKey(cacheKey)).thenReturn(true);
        when(cacheManager.getLocationList(cacheKey)).thenReturn(cachedLocations);

        List<LocationResponseDto> result = ipLocationService.findByUsername("testuser");

        assertEquals(cachedLocations, result);
        verify(locationRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("Должен возвращать локации по имени пользователя из репозитория, если кэш пуст")
    void shouldReturnLocationsByUsernameFromRepository() {
        String cacheKey = "findByUsername:testuser";
        when(cacheManager.containsLocationListKey(cacheKey)).thenReturn(false);
        when(locationRepository.findByUsername("testuser")).thenReturn(List.of(location));

        List<LocationResponseDto> result = ipLocationService.findByUsername("testuser");

        assertEquals(1, result.size());
        assertEquals("192.168.1.1", result.get(0).getIpAddress());
        verify(cacheManager).putLocationList(cacheKey, result);
    }

    @Test
    @DisplayName("Должен успешно создавать локацию")
    void shouldCreateLocationSuccessfully() {
        when(ipAddressValidator.isValidIpAddress("192.168.1.1")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(restTemplate.getForObject(anyString(), eq(Location.class))).thenReturn(location);
        when(locationRepository.save(any(Location.class))).thenReturn(location);

        LocationResponseDto result = ipLocationService.create("192.168.1.1", userDto);

        assertEquals("192.168.1.1", result.getIpAddress());
        verify(cacheManager).invalidateLocationCache(1L, "testuser");
    }

    @Test
    @DisplayName("Должен бросать исключение при создании локации с неверным IP")
    void shouldThrowExceptionWhenCreatingWithInvalidIp() {
        when(ipAddressValidator.isValidIpAddress("invalid")).thenReturn(false);

        assertThrows(ResponseStatusException.class, () -> ipLocationService.create("invalid", userDto));
        verify(userRepository, never()).findByUsername(any());
    }

    @Test
    @DisplayName("Должен бросать исключение при создании локации с несуществующим пользователем")
    void shouldThrowExceptionWhenCreatingWithNonExistentUser() {
        when(ipAddressValidator.isValidIpAddress("192.168.1.1")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());

        assertThrows(ResponseStatusException.class, () -> ipLocationService.create("192.168.1.1", userDto));
        verify(restTemplate, never()).getForObject(any(), any());
    }

    @Test
    @DisplayName("Должен бросать исключение, если API возвращает неполные данные")
    void shouldThrowExceptionWhenApiReturnsIncompleteData() {
        when(ipAddressValidator.isValidIpAddress("192.168.1.1")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        Location incompleteLocation = mock(Location.class);
        when(incompleteLocation.getCity()).thenReturn(null);
        when(incompleteLocation.getCountry()).thenReturn("Country");
        when(restTemplate.getForObject(anyString(), eq(Location.class))).thenReturn(incompleteLocation);

        assertThrows(ResponseStatusException.class, () -> ipLocationService.create("192.168.1.1", userDto));
        verify(locationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Должен успешно создавать несколько локаций")
    void shouldCreateMultipleLocationsSuccessfully() {
        Location location2 = mock(Location.class);
        LocationResponseDto locationDto2 = mock(LocationResponseDto.class);
        when(location2.getId()).thenReturn(2L);
        when(location2.getIpAddress()).thenReturn("192.168.1.2");
        when(location2.getCity()).thenReturn("City2");
        when(location2.getCountry()).thenReturn("Country2");
        when(location2.getUser()).thenReturn(user);
        when(locationDto2.getId()).thenReturn(2L);
        when(locationDto2.getIpAddress()).thenReturn("192.168.1.2");
        when(locationDto2.getCity()).thenReturn("City2");
        when(locationDto2.getCountry()).thenReturn("Country2");

        when(ipAddressValidator.isValidIpAddress("192.168.1.1")).thenReturn(true);
        when(ipAddressValidator.isValidIpAddress("192.168.1.2")).thenReturn(true);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(restTemplate.getForObject(anyString(), eq(Location.class))).thenReturn(location, location2);
        when(locationRepository.save(any(Location.class))).thenReturn(location, location2);

        List<String> ipAddresses = List.of("192.168.1.1", "192.168.1.2");
        List<LocationResponseDto> result = ipLocationService.createBulk(ipAddresses, userDto);

        assertEquals(2, result.size());
        assertEquals("192.168.1.1", result.get(0).getIpAddress());
        assertEquals("192.168.1.2", result.get(1).getIpAddress());
        verify(cacheManager, times(2)).invalidateLocationCache(anyLong(), eq("testuser"));
    }

    @Test
    @DisplayName("Должен пропускать невалидные IP при массовом создании")
    void shouldSkipInvalidIpsWhenCreatingBulk() {
        when(ipAddressValidator.isValidIpAddress("192.168.1.1")).thenReturn(true);
        when(ipAddressValidator.isValidIpAddress("invalid")).thenReturn(false);
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(restTemplate.getForObject(anyString(), eq(Location.class))).thenReturn(location);
        when(locationRepository.save(any(Location.class))).thenReturn(location);

        List<String> ipAddresses = List.of("192.168.1.1", "invalid");
        List<LocationResponseDto> result = ipLocationService.createBulk(ipAddresses, userDto);

        assertEquals(1, result.size());
        assertEquals("192.168.1.1", result.get(0).getIpAddress());
        verify(cacheManager).invalidateLocationCache(1L, "testuser");
    }

    @Test
    @DisplayName("Должен возвращать пустой список при массовом создании, если входной список IP пуст")
    void shouldReturnEmptyListWhenCreatingBulkWithEmptyList() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        List<LocationResponseDto> result = ipLocationService.createBulk(Collections.emptyList(), userDto);

        assertTrue(result.isEmpty());
        verify(restTemplate, never()).getForObject(any(), any());
        verify(locationRepository, never()).save(any());
        verify(cacheManager, never()).invalidateLocationCache(anyLong(), anyString());
    }

    @Test
    @DisplayName("Должен успешно обновлять локацию")
    void shouldUpdateLocationSuccessfully() {
        Location updatedLocation = mock(Location.class);
        when(updatedLocation.getId()).thenReturn(1L);
        when(updatedLocation.getIpAddress()).thenReturn("192.168.1.2");
        when(updatedLocation.getCity()).thenReturn("NewCity");
        when(updatedLocation.getCountry()).thenReturn("NewCountry");
        when(updatedLocation.getUser()).thenReturn(user);

        LocationResponseDto updatedDto = mock(LocationResponseDto.class);
        when(updatedDto.getId()).thenReturn(1L);
        when(updatedDto.getIpAddress()).thenReturn("192.168.1.2");
        when(updatedDto.getCity()).thenReturn("NewCity");
        when(updatedDto.getCountry()).thenReturn("NewCountry");

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(Location.class))).thenReturn(updatedLocation);

        LocationResponseDto result = ipLocationService.update(1L, updatedDto);

        assertEquals("192.168.1.2", result.getIpAddress());
        verify(cacheManager).invalidateLocationCache(1L, "testuser");
    }

    @Test
    @DisplayName("Должен бросать исключение при обновлении несуществующей локации")
    void shouldThrowExceptionWhenUpdatingNonExistentLocation() {
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ipLocationService.update(1L, locationDto));
    }

    @Test
    @DisplayName("Должен успешно удалять локацию")
    void shouldDeleteLocationSuccessfully() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));

        ipLocationService.delete(1L);

        verify(locationRepository).deleteById(1L);
        verify(cacheManager).invalidateLocationCache(1L, "testuser");
    }

    @Test
    @DisplayName("Должен бросать исключение при удалении несуществующей локации")
    void shouldThrowExceptionWhenDeletingNonExistentLocation() {
        when(locationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> ipLocationService.delete(1L));
    }
}