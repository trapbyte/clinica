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

import com.example.clinica.dtos.DoctorCreateDto;
import com.example.clinica.dtos.DoctorResponseDto;
import com.example.clinica.dtos.DoctorUpdateDto;
import com.example.clinica.mappers.DoctorMapper;
import com.example.clinica.models.Doctor;
import com.example.clinica.services.interf.IDoctorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final IDoctorService doctorService;

    public DoctorController(IDoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<List<DoctorResponseDto>> getAll() {
        List<Doctor> doctors = doctorService.getAll();
        List<DoctorResponseDto> doctorsDto = doctors.stream().map(DoctorMapper::modelToDto).toList();
        return ResponseEntity.ok(doctorsDto);
    }

    @GetMapping("/{license}")
    public ResponseEntity<DoctorResponseDto> getByLicense(@PathVariable("license") String license) {
        Doctor doctor = doctorService.getByLicense(license);
        return ResponseEntity.ok(DoctorMapper.modelToDto(doctor));
    }

    @PostMapping
    public ResponseEntity<DoctorResponseDto> create(@Valid @RequestBody DoctorCreateDto doctorCreateDto) {
        Doctor doctor = DoctorMapper.dtoToModel(doctorCreateDto);
        Doctor doctorCreated = doctorService.create(doctor);
        DoctorResponseDto dto = DoctorMapper.modelToDto(doctorCreated);
        return ResponseEntity.created(URI.create("/api/doctor/" + dto.getLicense())).body(dto);
    }

    @PutMapping("/{license}")
    public ResponseEntity<DoctorResponseDto> update(@PathVariable("license") String license,
            @Valid @RequestBody DoctorUpdateDto doctorUpdateDto) {
        Doctor doctor = DoctorMapper.doctorUpdateDtoToModel(doctorUpdateDto);
        Doctor doctorUpdated = doctorService.update(license, doctor);
        return ResponseEntity.ok(DoctorMapper.modelToDto(doctorUpdated));
    }

    @DeleteMapping("/{license}")
    public ResponseEntity<Void> delete(@PathVariable("license") String license) {
        doctorService.delete(license);
        return ResponseEntity.noContent().build();
    }
}
