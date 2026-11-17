package com.example.clinica.models;

import java.util.UUID;

import lombok.Data;

@Data
public class Doctor {
    private UUID id;
    private String license;
    private String fullName;
    private String specialty;
    private Double consultationFee;
}
