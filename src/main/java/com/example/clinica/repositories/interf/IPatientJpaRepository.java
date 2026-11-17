package com.example.clinica.repositories.interf;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clinica.entities.PatientEntity;

public interface IPatientJpaRepository extends JpaRepository<PatientEntity, UUID> {

    Optional<PatientEntity> findByDocument(String document);

    boolean existsByDocument(String document);
}
