package com.example.clinica.repositories.interf;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.models.AppointmentStatus;

public interface IAppointmentRepository {
    public Optional<AppointmentEntity> getByNumber(String number);

    public List<AppointmentEntity> getByDoctorAndDate(String doctorLicense, LocalDate date);

    public List<AppointmentEntity> getByPatientDocument(String document);

    public List<AppointmentEntity> getByDoctorId(UUID doctorId);

    public List<AppointmentEntity> getByPatientId(UUID patientId);

    public boolean existsForDoctorAt(UUID doctorId, LocalDateTime dateTime, AppointmentStatus status);

    public boolean existsForPatientOn(UUID patientId, LocalDate date, AppointmentStatus status);

    public boolean existsForDoctorWithStatus(UUID doctorId, AppointmentStatus status);

    public boolean existsForPatientWithStatus(UUID patientId, AppointmentStatus status);

    public int getLastNumberSequence();

    public AppointmentEntity create(AppointmentEntity appointmentEntity);

    public AppointmentEntity update(AppointmentEntity appointmentEntity);

    public void deleteAll(List<AppointmentEntity> appointmentEntities);
}
