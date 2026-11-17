package com.example.clinica.repositories.interf;

import java.util.List;
import java.util.Optional;

import com.example.clinica.entities.PatientEntity;

public interface IPatientRepository {
    public List<PatientEntity> getAll();

    public Optional<PatientEntity> getByDocument(String document);

    public boolean existsByDocument(String document);

    public PatientEntity create(PatientEntity patientEntity);

    public PatientEntity update(PatientEntity patientEntity);

    public void delete(PatientEntity patientEntity);
}
