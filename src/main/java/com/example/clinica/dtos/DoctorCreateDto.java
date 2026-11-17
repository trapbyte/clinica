package com.example.clinica.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class DoctorCreateDto {
    @NotBlank(message = "La licencia es obligatoria")
    private String license;

    @NotBlank(message = "El nombre completo es obligatorio")
    private String fullName;

    @NotBlank(message = "La especialidad es obligatoria")
    private String specialty;

    @NotNull(message = "La tarifa de consulta es obligatoria")
    @PositiveOrZero(message = "La tarifa de consulta no puede ser negativa")
    private Double consultationFee;
}
