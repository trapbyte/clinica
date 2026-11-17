package com.example.clinica.repositories.interf;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clinica.entities.DoctorEntity;

public interface IDoctorJpaRepository extends JpaRepository<DoctorEntity, UUID> {

    Optional<DoctorEntity> findByLicense(String license);

    boolean existsByLicense(String license);
}
