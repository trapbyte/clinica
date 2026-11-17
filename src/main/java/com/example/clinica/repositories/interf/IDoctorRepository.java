package com.example.clinica.repositories.interf;

import java.util.List;
import java.util.Optional;

import com.example.clinica.entities.DoctorEntity;

public interface IDoctorRepository {
    public List<DoctorEntity> getAll();

    public Optional<DoctorEntity> getByLicense(String license);

    public boolean existsByLicense(String license);

    public DoctorEntity create(DoctorEntity doctorEntity);

    public DoctorEntity update(DoctorEntity doctorEntity);

    public void delete(DoctorEntity doctorEntity);
}
