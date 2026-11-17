package com.example.clinica.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.entities.PatientEntity;
import com.example.clinica.exceptions.BusinessRuleException;
import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.exceptions.NotFoundException;
import com.example.clinica.models.Appointment;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.Doctor;
import com.example.clinica.models.InsuranceType;
import com.example.clinica.models.Patient;
import com.example.clinica.repositories.interf.IAppointmentRepository;
import com.example.clinica.repositories.interf.IDoctorRepository;
import com.example.clinica.repositories.interf.IPatientRepository;
import com.example.clinica.services.impl.AppointmentService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AppointmentServiceTest {

    // "Ahora" fijo: lunes 16 de noviembre de 2026, 7:00 a. m.
    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 16, 7, 0);
    // Lunes futuro de referencia
    private static final LocalDateTime MONDAY_9 = LocalDateTime.of(2026, 11, 23, 9, 0);

    @Mock
    IAppointmentRepository appointmentRepository;
    @Mock
    IDoctorRepository doctorRepository;
    @Mock
    IPatientRepository patientRepository;

    AppointmentService service;
    DoctorEntity doctor;
    PatientEntity patient;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        service = new AppointmentService(appointmentRepository, doctorRepository, patientRepository, clock);

        doctor = new DoctorEntity();
        doctor.setId(UUID.randomUUID());
        doctor.setLicense("MED-100");
        doctor.setFullName("Ana Gómez");
        doctor.setSpecialty("Medicina general");
        doctor.setConsultationFee(100000.0);

        patient = new PatientEntity();
        patient.setId(UUID.randomUUID());
        patient.setDocument("P-1");
        patient.setFullName("Carlos Ruiz");
        patient.setInsuranceType(InsuranceType.NONE);
        patient.setNoShowCount(0);

        when(doctorRepository.getByLicense("MED-100")).thenReturn(Optional.of(doctor));
        when(patientRepository.getByDocument("P-1")).thenReturn(Optional.of(patient));
        when(appointmentRepository.getLastNumberSequence()).thenReturn(0);
        when(appointmentRepository.create(any())).thenAnswer(inv -> inv.getArgument(0));
        when(appointmentRepository.update(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private Appointment request(String license, String document, LocalDateTime dateTime) {
        Doctor d = new Doctor();
        d.setLicense(license);
        Patient p = new Patient();
        p.setDocument(document);
        Appointment appointment = new Appointment();
        appointment.setDoctor(d);
        appointment.setPatient(p);
        appointment.setDateTime(dateTime);
        return appointment;
    }

    private AppointmentEntity existing(AppointmentStatus status, LocalDateTime dateTime) {
        AppointmentEntity entity = new AppointmentEntity();
        entity.setNumber("CITA-0001");
        entity.setDoctor(doctor);
        entity.setPatient(patient);
        entity.setDateTime(dateTime);
        entity.setStatus(status);
        entity.setAmountToPay(100000.0);
        when(appointmentRepository.getByNumber("CITA-0001")).thenReturn(Optional.of(entity));
        return entity;
    }

    @Test
    void agendaCitaValidaConNumeroEstadoYValor() {
        Appointment created = service.create(request("MED-100", "P-1", MONDAY_9));

        assertThat(created.getNumber()).isEqualTo("CITA-0001");
        assertThat(created.getStatus()).isEqualTo(AppointmentStatus.SCHEDULED);
        assertThat(created.getAmountToPay()).isEqualTo(100000.0);
    }

    @Test
    void rn6CalculaValorSegunSeguro() {
        patient.setInsuranceType(InsuranceType.BASIC);
        assertThat(service.create(request("MED-100", "P-1", MONDAY_9)).getAmountToPay()).isEqualTo(50000.0);

        patient.setInsuranceType(InsuranceType.FULL);
        assertThat(service.create(request("MED-100", "P-1", MONDAY_9)).getAmountToPay()).isEqualTo(0.0);
    }

    @Test
    void rn1RechazaMinutosDistintosDe00y30() {
        assertThatThrownBy(() -> service.create(request("MED-100", "P-1", MONDAY_9.withMinute(15))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rn2RechazaFinDeSemanaYFueraDeHorario() {
        assertThatThrownBy(() -> service.create(request("MED-100", "P-1", MONDAY_9.plusDays(5))))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("lunes a viernes");
        assertThatThrownBy(() -> service.create(request("MED-100", "P-1", MONDAY_9.withHour(17))))
                .isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> service.create(request("MED-100", "P-1", MONDAY_9.withHour(7).withMinute(30))))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void rn2AceptaLaUltimaCitaDeLas1630() {
        assertThat(service.create(request("MED-100", "P-1", MONDAY_9.withHour(16).withMinute(30))).getStatus())
                .isEqualTo(AppointmentStatus.SCHEDULED);
    }

    @Test
    void rn3RechazaFechasEnElPasado() {
        LocalDateTime lastFriday = LocalDateTime.of(2026, 11, 13, 9, 0);
        assertThatThrownBy(() -> service.create(request("MED-100", "P-1", lastFriday)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("pasado");
    }

    @Test
    void rn4MedicoOcupadoEsConflicto() {
        when(appointmentRepository.existsForDoctorAt(doctor.getId(), MONDAY_9, AppointmentStatus.SCHEDULED))
                .thenReturn(true);
        assertThatThrownBy(() -> service.create(request("MED-100", "P-1", MONDAY_9)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("El médico MED-100 ya tiene una cita en ese horario");
    }

    @Test
    void rn5PacienteConCitaEseDiaEsConflicto() {
        when(appointmentRepository.existsForPatientOn(patient.getId(), MONDAY_9.toLocalDate(),
                AppointmentStatus.SCHEDULED)).thenReturn(true);
        assertThatThrownBy(() -> service.create(request("MED-100", "P-1", MONDAY_9)))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void rn7PacienteConDosInasistenciasNoAgenda() {
        patient.setNoShowCount(2);
        assertThatThrownBy(() -> service.create(request("MED-100", "P-1", MONDAY_9)))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("inasistencias");
        verify(appointmentRepository, never()).create(any());
    }

    @Test
    void medicoInexistenteEsNotFound() {
        when(doctorRepository.getByLicense("MED-999")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.create(request("MED-999", "P-1", MONDAY_9)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void rn8CancelaConMasDeDosHoras() {
        existing(AppointmentStatus.SCHEDULED, NOW.plusHours(3));
        assertThat(service.cancel("CITA-0001").getStatus()).isEqualTo(AppointmentStatus.CANCELLED);
    }

    @Test
    void rn8NoCancelaConUnaHoraOExactamenteDosHoras() {
        AppointmentEntity entity = existing(AppointmentStatus.SCHEDULED, NOW.plusHours(1));
        assertThatThrownBy(() -> service.cancel("CITA-0001")).isInstanceOf(BusinessRuleException.class);

        entity.setDateTime(NOW.plusHours(2));
        assertThatThrownBy(() -> service.cancel("CITA-0001")).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void estadosFinalesNoPuedenCambiar() {
        existing(AppointmentStatus.CANCELLED, MONDAY_9);
        assertThatThrownBy(() -> service.attend("CITA-0001"))
                .isInstanceOf(BusinessRuleException.class).hasMessageContaining("CANCELLED");
        assertThatThrownBy(() -> service.markNoShow("CITA-0001")).isInstanceOf(BusinessRuleException.class);
        assertThatThrownBy(() -> service.cancel("CITA-0001")).isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void atenderCitaProgramada() {
        existing(AppointmentStatus.SCHEDULED, MONDAY_9);
        assertThat(service.attend("CITA-0001").getStatus()).isEqualTo(AppointmentStatus.ATTENDED);
    }

    @Test
    void noShowIncrementaInasistenciasDelPaciente() {
        existing(AppointmentStatus.SCHEDULED, MONDAY_9);
        Appointment result = service.markNoShow("CITA-0001");

        assertThat(result.getStatus()).isEqualTo(AppointmentStatus.NO_SHOW);
        assertThat(patient.getNoShowCount()).isEqualTo(1);
        verify(patientRepository).update(patient);
    }

    @Test
    void numeroDeCitaEsConsecutivo() {
        when(appointmentRepository.getLastNumberSequence()).thenReturn(41);
        assertThat(service.create(request("MED-100", "P-1", MONDAY_9)).getNumber()).isEqualTo("CITA-0042");
        verify(appointmentRepository).existsForDoctorAt(eq(doctor.getId()), eq(MONDAY_9), eq(AppointmentStatus.SCHEDULED));
    }
}
