package com.example.clinica.dtos;

import java.time.LocalDate;
import java.util.UUID;

import com.example.clinica.models.InsuranceType;

import lombok.Data;

@Data
public class PatientResponseDto {
    private UUID id;
    private String document;
    private String fullName;
    private LocalDate birthDate;
    private InsuranceType insuranceType;
    private Integer noShowCount;
}
