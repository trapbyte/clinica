package com.example.clinica.dtos;

import java.util.UUID;

import lombok.Data;

@Data
public class DoctorResponseDto {
    private UUID id;
    private String license;
    private String fullName;
    private String specialty;
    private Double consultationFee;
}
