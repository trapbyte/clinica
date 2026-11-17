package com.example.clinica.services.impl;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinica.entities.AppointmentEntity;
import com.example.clinica.entities.DoctorEntity;
import com.example.clinica.entities.PatientEntity;
import com.example.clinica.exceptions.BusinessRuleException;
import com.example.clinica.exceptions.ConflictException;
import com.example.clinica.exceptions.NotFoundException;
import com.example.clinica.mappers.AppointmentMapper;
import com.example.clinica.models.Appointment;
import com.example.clinica.models.AppointmentStatus;
import com.example.clinica.models.InsuranceType;
import com.example.clinica.repositories.interf.IAppointmentRepository;
import com.example.clinica.repositories.interf.IDoctorRepository;
import com.example.clinica.repositories.interf.IPatientRepository;
import com.example.clinica.services.interf.IAppointmentService;

@Service
public class AppointmentService implements IAppointmentService {

    private static final LocalTime FIRST_SLOT = LocalTime.of(8, 0);
    private static final LocalTime LAST_SLOT = LocalTime.of(16, 30);
    private static final Duration MIN_CANCEL_NOTICE = Duration.ofHours(2);
    private static final int MAX_NO_SHOWS = 2;

    private final IAppointmentRepository appointmentRepository;
    private final IDoctorRepository doctorRepository;
    private final IPatientRepository patientRepository;
    private final Clock clock;

    public AppointmentService(IAppointmentRepository appointmentRepository, IDoctorRepository doctorRepository,
            IPatientRepository patientRepository, Clock clock) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public Appointment create(Appointment appointment) {
        DoctorEntity doctor = findDoctor(appointment.getDoctor().getLicense());
        PatientEntity patient = findPatient(appointment.getPatient().getDocument());
        LocalDateTime dateTime = appointment.getDateTime();

        // RN1: bloques de 30 minutos que empiezan en :00 o :30
        boolean onSlot = (dateTime.getMinute() == 0 || dateTime.getMinute() == 30)
                && dateTime.getSecond() == 0 && dateTime.getNano() == 0;
        if (!onSlot) {
            throw new BusinessRuleException("Las citas duran 30 minutos y sólo inician a las :00 o a las :30");
        }

        // RN2: lunes a viernes, de 8:00 a 17:00 (la última inicia 16:30)
        DayOfWeek day = dateTime.getDayOfWeek();
        if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
            throw new BusinessRuleException("Sólo se agendan citas de lunes a viernes");
        }
        LocalTime time = dateTime.toLocalTime();
        if (time.isBefore(FIRST_SLOT) || time.isAfter(LAST_SLOT)) {
            throw new BusinessRuleException("El horario de atención es de 8:00 a 17:00; la última cita inicia a las 16:30");
        }

        // RN3: no se agenda en el pasado
        if (!dateTime.isAfter(LocalDateTime.now(clock))) {
            throw new BusinessRuleException("No se puede agendar una cita en el pasado");
        }

        // RN7: pacientes con 2 o más inasistencias quedan bloqueados
        if (patient.getNoShowCount() >= MAX_NO_SHOWS) {
            throw new BusinessRuleException("El paciente " + patient.getDocument() + " tiene "
                    + patient.getNoShowCount() + " inasistencias y no puede agendar citas");
        }

        // RN4: el médico no puede tener dos citas SCHEDULED en el mismo horario
        if (appointmentRepository.existsForDoctorAt(doctor.getId(), dateTime, AppointmentStatus.SCHEDULED)) {
            throw new ConflictException("El médico " + doctor.getLicense() + " ya tiene una cita en ese horario");
        }

        // RN5: el paciente no puede tener dos citas SCHEDULED el mismo día
        if (appointmentRepository.existsForPatientOn(patient.getId(), dateTime.toLocalDate(),
                AppointmentStatus.SCHEDULED)) {
            throw new ConflictException("El paciente " + patient.getDocument() + " ya tiene una cita el "
                    + dateTime.toLocalDate());
        }

        AppointmentEntity appointmentEntity = new AppointmentEntity();
        appointmentEntity.setNumber(nextNumber());
        appointmentEntity.setDoctor(doctor);
        appointmentEntity.setPatient(patient);
        appointmentEntity.setDateTime(dateTime);
        appointmentEntity.setStatus(AppointmentStatus.SCHEDULED);
        appointmentEntity.setAmountToPay(calculateAmountToPay(doctor.getConsultationFee(), patient.getInsuranceType()));

        AppointmentEntity createdAppointmentEntity = appointmentRepository.create(appointmentEntity);
        return AppointmentMapper.entityToModel(createdAppointmentEntity);
    }

    @Override
    public Appointment getByNumber(String number) {
        return AppointmentMapper.entityToModel(findAppointment(number));
    }

    @Override
    @Transactional
    public Appointment cancel(String number) {
        AppointmentEntity appointmentEntity = findAppointment(number);
        ensureScheduled(appointmentEntity, AppointmentStatus.CANCELLED);

        // RN8: sólo con más de 2 horas de anticipación
        Duration timeLeft = Duration.between(LocalDateTime.now(clock), appointmentEntity.getDateTime());
        if (timeLeft.compareTo(MIN_CANCEL_NOTICE) <= 0) {
            throw new BusinessRuleException("La cita " + number
                    + " sólo se puede cancelar con más de 2 horas de anticipación");
        }

        appointmentEntity.setStatus(AppointmentStatus.CANCELLED);
        return AppointmentMapper.entityToModel(appointmentRepository.update(appointmentEntity));
    }

    @Override
    @Transactional
    public Appointment attend(String number) {
        AppointmentEntity appointmentEntity = findAppointment(number);
        ensureScheduled(appointmentEntity, AppointmentStatus.ATTENDED);

        appointmentEntity.setStatus(AppointmentStatus.ATTENDED);
        return AppointmentMapper.entityToModel(appointmentRepository.update(appointmentEntity));
    }

    @Override
    @Transactional
    public Appointment markNoShow(String number) {
        AppointmentEntity appointmentEntity = findAppointment(number);
        ensureScheduled(appointmentEntity, AppointmentStatus.NO_SHOW);

        appointmentEntity.setStatus(AppointmentStatus.NO_SHOW);
        PatientEntity patient = appointmentEntity.getPatient();
        patient.setNoShowCount(patient.getNoShowCount() + 1);
        patientRepository.update(patient);

        return AppointmentMapper.entityToModel(appointmentRepository.update(appointmentEntity));
    }

    @Override
    public List<Appointment> getByDoctorAndDate(String doctorLicense, LocalDate date) {
        findDoctor(doctorLicense);
        return appointmentRepository.getByDoctorAndDate(doctorLicense, date).stream()
                .map(AppointmentMapper::entityToModel)
                .toList();
    }

    @Override
    public List<Appointment> getByPatient(String document) {
        findPatient(document);
        return appointmentRepository.getByPatientDocument(document).stream()
                .map(AppointmentMapper::entityToModel)
                .toList();
    }

    // RN6: el seguro define qué parte de la tarifa paga el paciente
    private double calculateAmountToPay(double consultationFee, InsuranceType insuranceType) {
        return switch (insuranceType) {
            case NONE -> consultationFee;
            case BASIC -> consultationFee * 0.5;
            case FULL -> 0.0;
        };
    }

    // Máquina de estados: sólo una cita SCHEDULED puede pasar a ATTENDED, CANCELLED o NO_SHOW
    private void ensureScheduled(AppointmentEntity appointmentEntity, AppointmentStatus target) {
        if (appointmentEntity.getStatus() != AppointmentStatus.SCHEDULED) {
            throw new BusinessRuleException("La cita " + appointmentEntity.getNumber() + " está en estado "
                    + appointmentEntity.getStatus() + " y no puede pasar a " + target);
        }
    }

    private String nextNumber() {
        return String.format("CITA-%04d", appointmentRepository.getLastNumberSequence() + 1);
    }

    private DoctorEntity findDoctor(String license) {
        return doctorRepository.getByLicense(license)
                .orElseThrow(() -> new NotFoundException("No existe un médico con licencia " + license));
    }

    private PatientEntity findPatient(String document) {
        return patientRepository.getByDocument(document)
                .orElseThrow(() -> new NotFoundException("No existe un paciente con documento " + document));
    }

    private AppointmentEntity findAppointment(String number) {
        return appointmentRepository.getByNumber(number)
                .orElseThrow(() -> new NotFoundException("No existe una cita con número " + number));
    }
}
