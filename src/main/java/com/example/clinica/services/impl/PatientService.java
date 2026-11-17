package com.example.clinica.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinica.entities.PatientEntity;
import com.example.clinica.exceptions.BusinessRuleException;
import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.exceptions.NotFoundException;
import com.example.clinica.mappers.PatientMapper;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.Patient;
import com.example.clinica.repositories.interf.IAppointmentRepository;
import com.example.clinica.repositories.interf.IPatientRepository;
import com.example.clinica.services.interf.IPatientService;

@Service
public class PatientService implements IPatientService {

    private final IPatientRepository patientRepository;
    private final IAppointmentRepository appointmentRepository;

    public PatientService(IPatientRepository patientRepository, IAppointmentRepository appointmentRepository) {
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public List<Patient> getAll() {
        List<PatientEntity> patientsEntity = patientRepository.getAll();
        return patientsEntity.stream().map(PatientMapper::entityToModel).toList();
    }

    @Override
    public Patient getByDocument(String document) {
        return PatientMapper.entityToModel(findEntity(document));
    }

    @Override
    @Transactional
    public Patient create(Patient patient) {
        if (patientRepository.existsByDocument(patient.getDocument())) {
            throw new ConflictException("Ya existe un paciente con documento " + patient.getDocument());
        }
        patient.setId(null);
        patient.setNoShowCount(0);
        PatientEntity patientEntity = PatientMapper.modelToEntity(patient);
        PatientEntity createdPatientEntity = patientRepository.create(patientEntity);
        return PatientMapper.entityToModel(createdPatientEntity);
    }

    @Override
    @Transactional
    public Patient update(String document, Patient patient) {
        PatientEntity patientEntity = findEntity(document);
        patientEntity.setFullName(patient.getFullName());
        patientEntity.setBirthDate(patient.getBirthDate());
        patientEntity.setInsuranceType(patient.getInsuranceType());
        PatientEntity updatedPatientEntity = patientRepository.update(patientEntity);
        return PatientMapper.entityToModel(updatedPatientEntity);
    }

    @Override
    @Transactional
    public void delete(String document) {
        PatientEntity patientEntity = findEntity(document);
        if (appointmentRepository.existsForPatientWithStatus(patientEntity.getId(), AppointmentStatus.SCHEDULED)) {
            throw new BusinessRuleException("El paciente " + document
                    + " tiene citas programadas; cancélelas o márquelas antes de eliminarlo");
        }
        // Sólo quedan citas finalizadas (historial): se eliminan junto con el paciente
        appointmentRepository.deleteAll(appointmentRepository.getByPatientId(patientEntity.getId()));
        patientRepository.delete(patientEntity);
    }

    private PatientEntity findEntity(String document) {
        return patientRepository.getByDocument(document)
                .orElseThrow(() -> new NotFoundException("No existe un paciente con documento " + document));
    }
}
