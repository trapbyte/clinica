package com.example.clinica.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.models.InsuranceType;
import com.example.clinica.models.Patient;
import com.example.clinica.repositories.interf.IAppointmentRepository;
import com.example.clinica.repositories.interf.IPatientRepository;
import com.example.clinica.services.impl.PatientService;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    IPatientRepository patientRepository;
    @Mock
    IAppointmentRepository appointmentRepository;
    @InjectMocks
    PatientService service;

    private Patient patient() {
        Patient patient = new Patient();
        patient.setDocument("P-1");
        patient.setFullName("Carlos Ruiz");
        patient.setInsuranceType(InsuranceType.NONE);
        patient.setNoShowCount(7);
        return patient;
    }

    @Test
    void documentoDuplicadoEsConflicto() {
        when(patientRepository.existsByDocument("P-1")).thenReturn(true);
        assertThatThrownBy(() -> service.create(patient())).isInstanceOf(ConflictException.class);
    }

    @Test
    void pacienteNuevoEmpiezaSinInasistencias() {
        when(patientRepository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.create(patient()).getNoShowCount()).isZero();
    }
}
