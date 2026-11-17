package com.example.clinica.services.interf;

import java.util.List;

import com.example.clinica.models.Doctor;

public interface IDoctorService {
    public List<Doctor> getAll();

    public Doctor getByLicense(String license);

    public Doctor create(Doctor doctor);

    public Doctor update(String license, Doctor doctor);

    public void delete(String license);
}
