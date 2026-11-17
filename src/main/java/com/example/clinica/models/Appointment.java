package com.example.clinica.models;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.Data;

@Data
public class Appointment {
    private UUID id;
    private String number;
    private Doctor doctor;
    private Patient patient;
    private LocalDateTime dateTime;
    private AppointmentStatus status;
    private Double amountToPay;
}
