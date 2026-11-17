package com.example.clinica.controllers;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.clinica.dtos.PatientCreateDto;
import com.example.clinica.dtos.PatientResponseDto;
import com.example.clinica.dtos.PatientUpdateDto;
import com.example.clinica.mappers.PatientMapper;
import com.example.clinica.models.Patient;
import com.example.clinica.services.interf.IPatientService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final IPatientService patientService;

    public PatientController(IPatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDto>> getAll() {
        List<Patient> patients = patientService.getAll();
        List<PatientResponseDto> patientsDto = patients.stream().map(PatientMapper::modelToDto).toList();
        return ResponseEntity.ok(patientsDto);
    }

    @GetMapping("/{document}")
    public ResponseEntity<PatientResponseDto> getByDocument(@PathVariable("document") String document) {
        Patient patient = patientService.getByDocument(document);
        return ResponseEntity.ok(PatientMapper.modelToDto(patient));
    }

    @PostMapping
    public ResponseEntity<PatientResponseDto> create(@Valid @RequestBody PatientCreateDto patientCreateDto) {
        Patient patient = PatientMapper.dtoToModel(patientCreateDto);
        Patient patientCreated = patientService.create(patient);
        PatientResponseDto dto = PatientMapper.modelToDto(patientCreated);
        return ResponseEntity.created(URI.create("/api/patient/" + dto.getDocument())).body(dto);
    }

    @PutMapping("/{document}")
    public ResponseEntity<PatientResponseDto> update(@PathVariable("document") String document,
            @Valid @RequestBody PatientUpdateDto patientUpdateDto) {
        Patient patient = PatientMapper.patientUpdateDtoToModel(patientUpdateDto);
        Patient patientUpdated = patientService.update(document, patient);
        return ResponseEntity.ok(PatientMapper.modelToDto(patientUpdated));
    }

    @DeleteMapping("/{document}")
    public ResponseEntity<Void> delete(@PathVariable("document") String document) {
        patientService.delete(document);
        return ResponseEntity.noContent().build();
    }
}
