package com.example.clinica.mappers;

import com.example.clinica.dtos.PatientCreateDto;
import com.example.clinica.dtos.PatientResponseDto;
import com.example.clinica.dtos.PatientUpdateDto;
import com.example.clinica.entities.PatientEntity;
import com.example.clinica.models.Patient;

public class PatientMapper {

    public static PatientResponseDto modelToDto(Patient patient) {
        PatientResponseDto dto = new PatientResponseDto();
        dto.setId(patient.getId());
        dto.setDocument(patient.getDocument());
        dto.setFullName(patient.getFullName());
        dto.setBirthDate(patient.getBirthDate());
        dto.setInsuranceType(patient.getInsuranceType());
        dto.setNoShowCount(patient.getNoShowCount());
        return dto;
    }

    public static Patient entityToModel(PatientEntity patientEntity) {
        Patient patient = new Patient();
        patient.setId(patientEntity.getId());
        patient.setDocument(patientEntity.getDocument());
        patient.setFullName(patientEntity.getFullName());
        patient.setBirthDate(patientEntity.getBirthDate());
        patient.setInsuranceType(patientEntity.getInsuranceType());
        patient.setNoShowCount(patientEntity.getNoShowCount());
        return patient;
    }

    public static PatientEntity modelToEntity(Patient patient) {
        PatientEntity patientEntity = new PatientEntity();
        patientEntity.setId(patient.getId());
        patientEntity.setDocument(patient.getDocument());
        patientEntity.setFullName(patient.getFullName());
        patientEntity.setBirthDate(patient.getBirthDate());
        patientEntity.setInsuranceType(patient.getInsuranceType());
        patientEntity.setNoShowCount(patient.getNoShowCount());
        return patientEntity;
    }

    public static Patient dtoToModel(PatientCreateDto patientCreateDto) {
        Patient patient = new Patient();
        patient.setDocument(patientCreateDto.getDocument());
        patient.setFullName(patientCreateDto.getFullName());
        patient.setBirthDate(patientCreateDto.getBirthDate());
        patient.setInsuranceType(patientCreateDto.getInsuranceType());
        return patient;
    }

    public static Patient patientUpdateDtoToModel(PatientUpdateDto patientUpdateDto) {
        Patient patient = new Patient();
        patient.setFullName(patientUpdateDto.getFullName());
        patient.setBirthDate(patientUpdateDto.getBirthDate());
        patient.setInsuranceType(patientUpdateDto.getInsuranceType());
        return patient;
    }
}
