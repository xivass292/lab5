package com.example.javalabaip.controller;

import com.example.javalabaip.cache.CacheManager;
import com.example.javalabaip.dto.LocationResponseDto;
import com.example.javalabaip.dto.UserDto;
import com.example.javalabaip.repository.LocationRepository;
import com.example.javalabaip.service.IpLocationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class IpLocationController {

    private final IpLocationService ipLocationService;
    private final LocationRepository locationRepository;
    private final CacheManager cacheManager;

    @Autowired
    public IpLocationController(IpLocationService ipLocationService, LocationRepository locationRepository, CacheManager cacheManager) {
        this.ipLocationService = ipLocationService;
        this.locationRepository = locationRepository;
        this.cacheManager = cacheManager;
    }

    @PostMapping("/location")
    public ResponseEntity<LocationResponseDto> createLocation(@RequestParam("ip") String ipAddress, @Valid @RequestBody UserDto userDto) {
        LocationResponseDto response = ipLocationService.create(ipAddress, userDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/locations/bulk")
    public ResponseEntity<List<LocationResponseDto>> createBulkLocations(@Valid @RequestBody Map<String, Object> request) {
        @SuppressWarnings("unchecked")
        List<String> ipAddresses = (List<String>) request.get("ipAddresses");
        @SuppressWarnings("unchecked")
        Map<String, Object> userDtoMap = (Map<String, Object>) request.get("userDto");
        UserDto userDto = new UserDto();
        userDto.setUsername((String) userDtoMap.get("username"));

        if (ipAddresses == null || userDto.getUsername() == null) {
            return ResponseEntity.badRequest().build();
        }

        List<LocationResponseDto> responses = ipLocationService.createBulk(ipAddresses, userDto);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/locations/{id}")
    public ResponseEntity<LocationResponseDto> findById(@PathVariable Long id) {
        LocationResponseDto location = ipLocationService.findById(id);
        return location != null ? ResponseEntity.ok(location) : ResponseEntity.notFound().build();
    }

    @GetMapping("/locations")
    public ResponseEntity<List<LocationResponseDto>> findAll() {
        List<LocationResponseDto> locations = ipLocationService.findAll();
        return ResponseEntity.ok(locations);
    }

    @GetMapping("/locations/by-username")
    public ResponseEntity<List<LocationResponseDto>> findByUsername(@RequestParam("username") String username) {
        List<LocationResponseDto> locations = ipLocationService.findByUsername(username);
        return ResponseEntity.ok(locations);
    }

    @PutMapping("/locations/{id}")
    public ResponseEntity<LocationResponseDto> update(@PathVariable Long id, @Valid @RequestBody LocationResponseDto locationDto) {
        LocationResponseDto updatedLocation = ipLocationService.update(id, locationDto);
        return ResponseEntity.ok(updatedLocation);
    }

    @DeleteMapping("/locations/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        ipLocationService.delete(id);
        return ResponseEntity.noContent().build();
    }
}