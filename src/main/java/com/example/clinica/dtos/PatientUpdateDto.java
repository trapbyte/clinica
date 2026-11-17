package com.example.clinica.dtos;

import java.time.LocalDate;

import com.example.clinica.models.InsuranceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;

@Data
public class PatientUpdateDto {
    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    @NotNull(message = "La fecha de nacimiento es obligatoria")
    @PastOrPresent(message = "La fecha de nacimiento no puede estar en el futuro")
    private LocalDate birthDate;

    @NotNull(message = "El tipo de seguro es obligatorio (NONE, BASIC o FULL)")
    private InsuranceType insuranceType;
}
