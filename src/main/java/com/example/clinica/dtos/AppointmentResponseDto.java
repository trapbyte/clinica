package com.example.clinica.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.InsuranceType;

import lombok.Data;

@Data
public class AppointmentResponseDto {
    private UUID id;
    private String number;
    private String doctorLicense;
    private String doctorName;
    private String specialty;
    private String patientDocument;
    private String patientName;
    private InsuranceType insuranceType;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private Double amountToPay;
}
