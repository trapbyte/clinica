package com.example.clinica.controllers;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.clinica.dtos.AppointmentCreateDto;
import com.example.clinica.dtos.AppointmentResponseDto;
import com.example.clinica.mappers.AppointmentMapper;
import com.example.clinica.models.Appointment;
import com.example.clinica.services.interf.IAppointmentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/appointment")
public class AppointmentController {

    private final IAppointmentService appointmentService;

    public AppointmentController(IAppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping
    public ResponseEntity<AppointmentResponseDto> create(
            @Valid @RequestBody AppointmentCreateDto appointmentCreateDto) {
        Appointment appointment = AppointmentMapper.dtoToModel(appointmentCreateDto);
        Appointment appointmentCreated = appointmentService.create(appointment);
        AppointmentResponseDto dto = AppointmentMapper.modelToDto(appointmentCreated);
        return ResponseEntity.created(URI.create("/api/appointment/" + dto.getNumber())).body(dto);
    }

    @GetMapping("/{number}")
    public ResponseEntity<AppointmentResponseDto> getByNumber(@PathVariable("number") String number) {
        Appointment appointment = appointmentService.getByNumber(number);
        return ResponseEntity.ok(AppointmentMapper.modelToDto(appointment));
    }

    @GetMapping
    public ResponseEntity<List<AppointmentResponseDto>> getByDoctorAndDate(
            @RequestParam("doctorLicense") String doctorLicense,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<Appointment> appointments = appointmentService.getByDoctorAndDate(doctorLicense, date);
        return ResponseEntity.ok(appointments.stream().map(AppointmentMapper::modelToDto).toList());
    }

    @GetMapping("/patient/{document}")
    public ResponseEntity<List<AppointmentResponseDto>> getByPatient(@PathVariable("document") String document) {
        List<Appointment> appointments = appointmentService.getByPatient(document);
        return ResponseEntity.ok(appointments.stream().map(AppointmentMapper::modelToDto).toList());
    }

    @PatchMapping("/{number}/cancel")
    public ResponseEntity<AppointmentResponseDto> cancel(@PathVariable("number") String number) {
        Appointment appointment = appointmentService.cancel(number);
        return ResponseEntity.ok(AppointmentMapper.modelToDto(appointment));
    }

    @PatchMapping("/{number}/attend")
    public ResponseEntity<AppointmentResponseDto> attend(@PathVariable("number") String number) {
        Appointment appointment = appointmentService.attend(number);
        return ResponseEntity.ok(AppointmentMapper.modelToDto(appointment));
    }

    @PatchMapping("/{number}/no-show")
    public ResponseEntity<AppointmentResponseDto> markNoShow(@PathVariable("number") String number) {
        Appointment appointment = appointmentService.markNoShow(number);
        return ResponseEntity.ok(AppointmentMapper.modelToDto(appointment));
    }
}
