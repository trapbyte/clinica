package com.example.clinica.repositories.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.repositories.interf.IDoctorJpaRepository;
import com.example.clinica.repositories.interf.IDoctorRepository;

@Repository
public class DoctorRepository implements IDoctorRepository {

    private final IDoctorJpaRepository repository;

    public DoctorRepository(IDoctorJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<DoctorEntity> getAll() {
        return repository.findAll();
    }

    @Override
    public Optional<DoctorEntity> getByLicense(String license) {
        return repository.findByLicense(license);
    }

    @Override
    public boolean existsByLicense(String license) {
        return repository.existsByLicense(license);
    }

    @Override
    public DoctorEntity create(DoctorEntity doctorEntity) {
        return repository.save(doctorEntity);
    }

    @Override
    public DoctorEntity update(DoctorEntity doctorEntity) {
        return repository.save(doctorEntity);
    }

    @Override
    public void delete(DoctorEntity doctorEntity) {
        repository.delete(doctorEntity);
    }
}
