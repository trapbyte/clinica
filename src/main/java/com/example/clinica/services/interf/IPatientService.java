package com.example.clinica.services.interf;

import java.util.List;

import com.example.clinica.models.Patient;

public interface IPatientService {
    public List<Patient> getAll();

    public Patient getByDocument(String document);

    public Patient create(Patient patient);

    public Patient update(String document, Patient patient);

    public void delete(String document);
}
