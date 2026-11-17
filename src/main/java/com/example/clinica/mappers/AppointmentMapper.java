package com.example.clinica.mappers;

import com.example.clinica.dtos.AppointmentCreateDto;
import com.example.clinica.dtos.AppointmentResponseDto;
import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.models.Appointment;
import com.example.clinica.models.Doctor;
import com.example.clinica.models.Patient;

public class AppointmentMapper {

    public static AppointmentResponseDto modelToDto(Appointment appointment) {
        AppointmentResponseDto dto = new AppointmentResponseDto();
        dto.setId(appointment.getId());
        dto.setNumber(appointment.getNumber());
        dto.setDoctorLicense(appointment.getDoctor().getLicense());
        dto.setDoctorName(appointment.getDoctor().getFullName());
        dto.setSpecialty(appointment.getDoctor().getSpecialty());
        dto.setPatientDocument(appointment.getPatient().getDocument());
        dto.setPatientName(appointment.getPatient().getFullName());
        dto.setInsuranceType(appointment.getPatient().getInsuranceType());
        dto.setDateTime(appointment.getDateTime());
        dto.setStatus(appointment.getStatus());
        dto.setAmountToPay(appointment.getAmountToPay());
        return dto;
    }

    public static Appointment entityToModel(AppointmentEntity appointmentEntity) {
        Appointment appointment = new Appointment();
        appointment.setId(appointmentEntity.getId());
        appointment.setNumber(appointmentEntity.getNumber());
        appointment.setDoctor(DoctorMapper.entityToModel(appointmentEntity.getDoctor()));
        appointment.setPatient(PatientMapper.entityToModel(appointmentEntity.getPatient()));
        appointment.setDateTime(appointmentEntity.getDateTime());
        appointment.setStatus(appointmentEntity.getStatus());
        appointment.setAmountToPay(appointmentEntity.getAmountToPay());
        return appointment;
    }

    public static AppointmentEntity modelToEntity(Appointment appointment) {
        AppointmentEntity appointmentEntity = new AppointmentEntity();
        appointmentEntity.setId(appointment.getId());
        appointmentEntity.setNumber(appointment.getNumber());
        appointmentEntity.setDoctor(DoctorMapper.modelToEntity(appointment.getDoctor()));
        appointmentEntity.setPatient(PatientMapper.modelToEntity(appointment.getPatient()));
        appointmentEntity.setDateTime(appointment.getDateTime());
        appointmentEntity.setStatus(appointment.getStatus());
        appointmentEntity.setAmountToPay(appointment.getAmountToPay());
        return appointmentEntity;
    }

    // El cliente sólo envía la licencia y el documento; el servicio completa médico y paciente
    public static Appointment dtoToModel(AppointmentCreateDto appointmentCreateDto) {
        Doctor doctor = new Doctor();
        doctor.setLicense(appointmentCreateDto.getDoctorLicense());
        Patient patient = new Patient();
        patient.setDocument(appointmentCreateDto.getPatientDocument());

        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setDateTime(appointmentCreateDto.getDateTime());
        return appointment;
    }
}
