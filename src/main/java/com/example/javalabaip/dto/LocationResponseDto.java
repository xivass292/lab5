package com.example.javalabaip.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class LocationResponseDto {
    private Long id;

    @NotBlank(message = "IP address cannot be empty")
    @Pattern(regexp = "^([0-9]{1,3}\\.){3}[0-9]{1,3}$", message = "Invalid IP address format")
    private String ipAddress;

    @NotBlank(message = "City cannot be empty")
    private String city;

    @NotBlank(message = "Country cannot be empty")
    private String country;

    private String continent;
    private Double latitude;
    private Double longitude;
    private String timezone;
}