package com.example.clinica.repositories.interf;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.models.AppointmentStatus;

public interface IAppointmentJpaRepository extends JpaRepository<AppointmentEntity, UUID> {

    Optional<AppointmentEntity> findByNumber(String number);

    // RN4: ¿el médico ya tiene una cita con ese estado a esa hora?
    boolean existsByDoctorIdAndDateTimeAndStatus(UUID doctorId, LocalDateTime dateTime, AppointmentStatus status);

    // RN5: ¿el paciente ya tiene una cita con ese estado dentro del rango [start, end)?
    @Query("SELECT COUNT(a) > 0 FROM AppointmentEntity a "
            + "WHERE a.patient.id = :patientId AND a.status = :status "
            + "AND a.dateTime >= :start AND a.dateTime < :end")
    boolean existsPatientAppointmentBetween(@Param("patientId") UUID patientId,
            @Param("status") AppointmentStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Agenda de un médico en un rango [start, end)
    @Query("SELECT a FROM AppointmentEntity a "
            + "WHERE a.doctor.license = :license AND a.dateTime >= :start AND a.dateTime < :end "
            + "ORDER BY a.dateTime")
    List<AppointmentEntity> findDoctorAgenda(@Param("license") String license,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    // Historial de un paciente, de la más reciente a la más antigua
    List<AppointmentEntity> findByPatientDocumentOrderByDateTimeDesc(String document);

    boolean existsByDoctorIdAndStatus(UUID doctorId, AppointmentStatus status);

    boolean existsByPatientIdAndStatus(UUID patientId, AppointmentStatus status);

    List<AppointmentEntity> findByDoctorId(UUID doctorId);

    List<AppointmentEntity> findByPatientId(UUID patientId);

    // Mayor consecutivo usado en los números CITA-0001, CITA-0002, ...
    @Query("SELECT COALESCE(MAX(CAST(SUBSTRING(a.number, 6) AS Integer)), 0) FROM AppointmentEntity a")
    Integer findLastNumberSequence();
}
