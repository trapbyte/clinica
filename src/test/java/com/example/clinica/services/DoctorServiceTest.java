package com.example.clinica.services;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.exceptions.BusinessRuleException;
import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.exceptions.NotFoundException;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.Doctor;
import com.example.clinica.repositories.interf.IAppointmentRepository;
import com.example.clinica.repositories.interf.IDoctorRepository;
import com.example.clinica.services.impl.DoctorService;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    IDoctorRepository doctorRepository;
    @Mock
    IAppointmentRepository appointmentRepository;
    @InjectMocks
    DoctorService service;

    @Test
    void licenciaDuplicadaEsConflicto() {
        Doctor doctor = new Doctor();
        doctor.setLicense("MED-100");
        when(doctorRepository.existsByLicense("MED-100")).thenReturn(true);

        assertThatThrownBy(() -> service.create(doctor)).isInstanceOf(ConflictException.class);
        verify(doctorRepository, never()).create(any());
    }

    @Test
    void noEliminaMedicoConCitasProgramadas() {
        DoctorEntity entity = new DoctorEntity();
        entity.setId(UUID.randomUUID());
        when(doctorRepository.getByLicense("MED-100")).thenReturn(Optional.of(entity));
        when(appointmentRepository.existsForDoctorWithStatus(entity.getId(), AppointmentStatus.SCHEDULED))
                .thenReturn(true);

        assertThatThrownBy(() -> service.delete("MED-100")).isInstanceOf(BusinessRuleException.class);
        verify(doctorRepository, never()).delete(any());
    }

    @Test
    void eliminaMedicoSinCitasProgramadas() {
        DoctorEntity entity = new DoctorEntity();
        entity.setId(UUID.randomUUID());
        when(doctorRepository.getByLicense("MED-100")).thenReturn(Optional.of(entity));
        when(appointmentRepository.getByDoctorId(entity.getId())).thenReturn(List.of());

        service.delete("MED-100");
        verify(doctorRepository).delete(entity);
    }

    @Test
    void actualizarMedicoInexistenteEsNotFound() {
        when(doctorRepository.getByLicense("MED-999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.update("MED-999", new Doctor())).isInstanceOf(NotFoundException.class);
    }
}
