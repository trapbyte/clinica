package com.example.clinica.services.interf;

import java.time.LocalDate;
import java.util.List;

import com.example.clinica.models.Appointment;

public interface IAppointmentService {
    public Appointment create(Appointment appointment);

    public Appointment getByNumber(String number);

    public Appointment cancel(String number);

    public Appointment attend(String number);

    public Appointment markNoShow(String number);

    public List<Appointment> getByDoctorAndDate(String doctorLicense, LocalDate date);

    public List<Appointment> getByPatient(String document);
}
