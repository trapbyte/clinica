package com.example.clinica.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.clinica.entities.PatientEntity;
import com.example.clinica.repositories.interf.IPatientJpaRepository;
import com.example.clinica.repositories.interf.IPatientRepository;

@Repository
public class PatientRepository implements IPatientRepository {

    private final IPatientJpaRepository repository;

    public PatientRepository(IPatientJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<PatientEntity> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<PatientEntity> getByDocument(String document) {
        return repository.findByDocument(document);
    }

    @Override
    public boolean existsByDocument(String document) {
        return repository.existsByDocument(document);
    }

    @Override
    public PatientEntity create(PatientEntity patientEntity) {
        return repository.save(patientEntity);
    }

    @Override
    public PatientEntity update(PatientEntity patientEntity) {
        return repository.save(patientEntity);
    }

    @Override
    public void delete(PatientEntity patientEntity) {
        repository.delete(patientEntity);
    }
}
