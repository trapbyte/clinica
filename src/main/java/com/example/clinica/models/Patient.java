package com.example.clinica.models;

import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

@Data
public class Patient {
    private UUID id;
    private String document;
    private String fullName;
    private LocalDate birthDate;
    private InsuranceType insuranceType;
    private Integer noShowCount;
}
