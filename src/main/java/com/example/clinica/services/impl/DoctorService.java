package com.example.clinica.services.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.exceptions.BusinessRuleException;
import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.exceptions.NotFoundException;
import com.example.clinica.mappers.DoctorMapper;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.Doctor;
import com.example.clinica.repositories.interf.IAppointmentRepository;
import com.example.clinica.repositories.interf.IDoctorRepository;
import com.example.clinica.services.interf.IDoctorService;

@Service
public class DoctorService implements IDoctorService {

    private final IDoctorRepository doctorRepository;
    private final IAppointmentRepository appointmentRepository;

    public DoctorService(IDoctorRepository doctorRepository, IAppointmentRepository appointmentRepository) {
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    public List<Doctor> getAll() {
        List<DoctorEntity> doctorsEntity = doctorRepository.getAll();
        return doctorsEntity.stream().map(DoctorMapper::entityToModel).toList();
    }

    @Override
    public Doctor getByLicense(String license) {
        return DoctorMapper.entityToModel(findEntity(license));
    }

    @Override
    @Transactional
    public Doctor create(Doctor doctor) {
        if (doctorRepository.existsByLicense(doctor.getLicense())) {
            throw new ConflictException("Ya existe un médico con licencia " + doctor.getLicense());
        }
        doctor.setId(null);
        DoctorEntity doctorEntity = DoctorMapper.modelToEntity(doctor);
        DoctorEntity createdDoctorEntity = doctorRepository.create(doctorEntity);
        return DoctorMapper.entityToModel(createdDoctorEntity);
    }

    @Override
    @Transactional
    public Doctor update(String license, Doctor doctor) {
        DoctorEntity doctorEntity = findEntity(license);
        doctorEntity.setFullName(doctor.getFullName());
        doctorEntity.setSpecialty(doctor.getSpecialty());
        doctorEntity.setConsultationFee(doctor.getConsultationFee());
        DoctorEntity updatedDoctorEntity = doctorRepository.update(doctorEntity);
        return DoctorMapper.entityToModel(updatedDoctorEntity);
    }

    @Override
    @Transactional
    public void delete(String license) {
        DoctorEntity doctorEntity = findEntity(license);
        if (appointmentRepository.existsForDoctorWithStatus(doctorEntity.getId(), AppointmentStatus.SCHEDULED)) {
            throw new BusinessRuleException("El médico " + license
                    + " tiene citas programadas; cancélelas o márquelas antes de eliminarlo");
        }
        // Sólo quedan citas finalizadas (historial): se eliminan junto con el médico
        appointmentRepository.deleteAll(appointmentRepository.getByDoctorId(doctorEntity.getId()));
        doctorRepository.delete(doctorEntity);
    }

    private DoctorEntity findEntity(String license) {
        return doctorRepository.getByLicense(license)
                .orElseThrow(() -> new NotFoundException("No existe un médico con licencia " + license));
    }
}
