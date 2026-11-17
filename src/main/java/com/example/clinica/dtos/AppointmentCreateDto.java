package com.example.clinica.dtos;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AppointmentCreateDto {
    @NotBlank(message = "La licencia del medico es obligatoria")
    private String doctorLicense;

    @NotBlank(message = "El documento del paciente es obligatorio")
    private String patientDocument;

    @NotNull(message = "La fecha y hora de la cita es obligatoria")
    private LocalDateTime dateTime;
}
