package com.example.clinica.repositories.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.repositories.interf.IAppointmentJpaRepository;
import com.example.clinica.repositories.interf.IAppointmentRepository;

@Repository
public class AppointmentRepository implements IAppointmentRepository {

    private final IAppointmentJpaRepository repository;

    public AppointmentRepository(IAppointmentJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<AppointmentEntity> getByNumber(String number) {
        return repository.findByNumber(number);
    }

    @Override
    public List<AppointmentEntity> getByDoctorAndDate(String doctorLicense, LocalDate date) {
        return repository.findDoctorAgenda(doctorLicense, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    @Override
    public List<AppointmentEntity> getByPatientDocument(String document) {
        return repository.findByPatientDocumentOrderByDateTimeDesc(document);
    }

    @Override
    public List<AppointmentEntity> getByDoctorId(UUID doctorId) {
        return repository.findByDoctorId(doctorId);
    }

    @Override
    public List<AppointmentEntity> getByPatientId(UUID patientId) {
        return repository.findByPatientId(patientId);
    }

    @Override
    public boolean existsForDoctorAt(UUID doctorId, LocalDateTime dateTime, AppointmentStatus status) {
        return repository.existsByDoctorIdAndDateTimeAndStatus(doctorId, dateTime, status);
    }

    @Override
    public boolean existsForPatientOn(UUID patientId, LocalDate date, AppointmentStatus status) {
        return repository.existsPatientAppointmentBetween(patientId, status,
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
    }

    @Override
    public boolean existsForDoctorWithStatus(UUID doctorId, AppointmentStatus status) {
        return repository.existsByDoctorIdAndStatus(doctorId, status);
    }

    @Override
    public boolean existsForPatientWithStatus(UUID patientId, AppointmentStatus status) {
        return repository.existsByPatientIdAndStatus(patientId, status);
    }

    @Override
    public int getLastNumberSequence() {
        return repository.findLastNumberSequence();
    }

    @Override
    public AppointmentEntity create(AppointmentEntity appointmentEntity) {
        return repository.save(appointmentEntity);
    }

    @Override
    public AppointmentEntity update(AppointmentEntity appointmentEntity) {
        return repository.save(appointmentEntity);
    }

    @Override
    public void deleteAll(List<AppointmentEntity> appointmentEntities) {
        repository.deleteAll(appointmentEntities);
    }
}
