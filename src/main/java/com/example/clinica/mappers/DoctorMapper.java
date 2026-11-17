package com.example.clinica.mappers;

import com.example.clinica.dtos.DoctorCreateDto;
import com.example.clinica.dtos.DoctorResponseDto;
import com.example.clinica.dtos.DoctorUpdateDto;
import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.models.Doctor;

public class DoctorMapper {

    public static DoctorResponseDto modelToDto(Doctor doctor) {
        DoctorResponseDto dto = new DoctorResponseDto();
        dto.setId(doctor.getId());
        dto.setLicense(doctor.getLicense());
        dto.setFullName(doctor.getFullName());
        dto.setSpecialty(doctor.getSpecialty());
        dto.setConsultationFee(doctor.getConsultationFee());
        return dto;
    }

    public static Doctor entityToModel(DoctorEntity doctorEntity) {
        Doctor doctor = new Doctor();
        doctor.setId(doctorEntity.getId());
        doctor.setLicense(doctorEntity.getLicense());
        doctor.setFullName(doctorEntity.getFullName());
        doctor.setSpecialty(doctorEntity.getSpecialty());
        doctor.setConsultationFee(doctorEntity.getConsultationFee());
        return doctor;
    }

    public static DoctorEntity modelToEntity(Doctor doctor) {
        DoctorEntity doctorEntity = new DoctorEntity();
        doctorEntity.setId(doctor.getId());
        doctorEntity.setLicense(doctor.getLicense());
        doctorEntity.setFullName(doctor.getFullName());
        doctorEntity.setSpecialty(doctor.getSpecialty());
        doctorEntity.setConsultationFee(doctor.getConsultationFee());
        return doctorEntity;
    }

    public static Doctor dtoToModel(DoctorCreateDto doctorCreateDto) {
        Doctor doctor = new Doctor();
        doctor.setLicense(doctorCreateDto.getLicense());
        doctor.setFullName(doctorCreateDto.getFullName());
        doctor.setSpecialty(doctorCreateDto.getSpecialty());
        doctor.setConsultationFee(doctorCreateDto.getConsultationFee());
        return doctor;
    }

    public static Doctor doctorUpdateDtoToModel(DoctorUpdateDto doctorUpdateDto) {
        Doctor doctor = new Doctor();
        doctor.setFullName(doctorUpdateDto.getFullName());
        doctor.setSpecialty(doctorUpdateDto.getSpecialty());
        doctor.setConsultationFee(doctorUpdateDto.getConsultationFee());
        return doctor;
    }
}
