package com.example.clinica.controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

/**
 * Recorre todas las capas (controlador, servicio, repositorio, JPA sobre H2) con los
 * casos de prueba de referencia de la sección 5 del taller.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ClinicaApiIntegrationTest {

    // "Ahora" fijo: lunes 16 de noviembre de 2026, 7:00 a. m.; el lunes futuro de referencia es el 23
    private static final ZoneId ZONE = ZoneId.of("America/Bogota");
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 11, 16, 7, 0);
    private static final String MONDAY = "2026-11-23";

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        @Primary
        Clock fixedClock() {
            return Clock.fixed(NOW.atZone(ZONE).toInstant(), ZONE);
        }
    }

    @Autowired
    MockMvc mockMvc;

    @BeforeEach
    void datosBase() throws Exception {
        postJson("/api/doctor", """
                {"license":"MED-100","fullName":"Ana Gómez","specialty":"Medicina general","consultationFee":100000}
                """).andExpect(status().isCreated());
        for (String[] p : new String[][] { { "P-1", "NONE" }, { "P-2", "BASIC" }, { "P-3", "FULL" } }) {
            postJson("/api/patient", """
                    {"document":"%s","fullName":"Paciente %s","birthDate":"1990-05-10","insuranceType":"%s"}
                    """.formatted(p[0], p[0], p[1])).andExpect(status().isCreated());
        }
    }

    private ResultActions postJson(String url, String body) throws Exception {
        return mockMvc.perform(post(url).contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private ResultActions schedule(String license, String document, String dateTime) throws Exception {
        return postJson("/api/appointment", """
                {"doctorLicense":"%s","patientDocument":"%s","dateTime":"%s"}
                """.formatted(license, document, dateTime));
    }

    // ---------- Sección 5: casos de referencia ----------

    @Test
    void citaValidaSeguroNone() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:00:00")
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/appointment/CITA-0001"))
                .andExpect(jsonPath("$.number").value("CITA-0001"))
                .andExpect(jsonPath("$.status").value("SCHEDULED"))
                .andExpect(jsonPath("$.amountToPay").value(100000.0));
    }

    @Test
    void seguroBasicPagaLaMitad() throws Exception {
        schedule("MED-100", "P-2", MONDAY + "T09:30:00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amountToPay").value(50000.0));
    }

    @Test
    void seguroFullNoPaga() throws Exception {
        schedule("MED-100", "P-3", MONDAY + "T10:00:00")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amountToPay").value(0.0));
    }

    @Test
    void minutoInvalido() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:15:00").andExpect(status().isUnprocessableContent());
    }

    @Test
    void sabado() throws Exception {
        schedule("MED-100", "P-1", "2026-11-28T09:00:00").andExpect(status().isUnprocessableContent());
    }

    @Test
    void fueraDeHorario() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T17:00:00").andExpect(status().isUnprocessableContent());
        schedule("MED-100", "P-1", MONDAY + "T07:30:00").andExpect(status().isUnprocessableContent());
    }

    @Test
    void enElPasado() throws Exception {
        schedule("MED-100", "P-1", "2026-11-13T09:00:00")
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.message").value("No se puede agendar una cita en el pasado"));
    }

    @Test
    void medicoOcupadoConFormatoDeError() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:00:00").andExpect(status().isCreated());
        schedule("MED-100", "P-2", MONDAY + "T09:00:00")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("El médico MED-100 ya tiene una cita en ese horario"))
                .andExpect(jsonPath("$.path").value("/api/appointment"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void pacienteConOtraCitaEseDia() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:00:00").andExpect(status().isCreated());
        schedule("MED-100", "P-1", MONDAY + "T11:00:00").andExpect(status().isConflict());
    }

    @Test
    void pacienteConDosNoShowNoAgenda() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:00:00").andExpect(status().isCreated());
        schedule("MED-100", "P-1", "2026-11-24T09:00:00").andExpect(status().isCreated());
        mockMvc.perform(patch("/api/appointment/CITA-0001/no-show")).andExpect(status().isOk());
        mockMvc.perform(patch("/api/appointment/CITA-0002/no-show"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
        mockMvc.perform(get("/api/patient/P-1")).andExpect(jsonPath("$.noShowCount").value(2));

        schedule("MED-100", "P-1", "2026-11-25T09:00:00").andExpect(status().isUnprocessableContent());
    }

    @Test
    void cancelarConUnaHoraDeAnticipacion() throws Exception {
        // Hoy (lunes 16) a las 8:00: falta 1 hora
        schedule("MED-100", "P-1", "2026-11-16T08:00:00").andExpect(status().isCreated());
        mockMvc.perform(patch("/api/appointment/CITA-0001/cancel")).andExpect(status().isUnprocessableContent());
    }

    @Test
    void atenderUnaCitaCancelada() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:00:00").andExpect(status().isCreated());
        mockMvc.perform(patch("/api/appointment/CITA-0001/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
        mockMvc.perform(patch("/api/appointment/CITA-0001/attend")).andExpect(status().isUnprocessableContent());
    }

    @Test
    void medicoInexistente() throws Exception {
        schedule("MED-999", "P-1", MONDAY + "T09:00:00")
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No existe un médico con licencia MED-999"));
    }

    // ---------- Estados ----------

    @Test
    void atenderCitaProgramada() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:00:00");
        mockMvc.perform(patch("/api/appointment/CITA-0001/attend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ATTENDED"));
    }

    @Test
    void horarioCanceladoQuedaLibre() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:00:00");
        mockMvc.perform(patch("/api/appointment/CITA-0001/cancel"));
        schedule("MED-100", "P-2", MONDAY + "T09:00:00").andExpect(status().isCreated());
    }

    // ---------- Consultas ----------

    @Test
    void agendaDelMedicoEnUnDia() throws Exception {
        schedule("MED-100", "P-2", MONDAY + "T10:00:00");
        schedule("MED-100", "P-1", MONDAY + "T09:00:00");
        schedule("MED-100", "P-3", "2026-11-24T09:00:00");

        mockMvc.perform(get("/api/appointment").param("doctorLicense", "MED-100").param("date", MONDAY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dateTime").value(MONDAY + "T09:00:00"))
                .andExpect(jsonPath("$[1].patientDocument").value("P-2"));
    }

    @Test
    void consultaDeAgendaSinParametrosEsBadRequest() throws Exception {
        mockMvc.perform(get("/api/appointment").param("doctorLicense", "MED-100"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void historialDelPaciente() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:00:00");
        schedule("MED-100", "P-1", "2026-11-24T09:00:00");

        mockMvc.perform(get("/api/appointment/patient/P-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].dateTime").value("2026-11-24T09:00:00"));
        mockMvc.perform(get("/api/appointment/CITA-0001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.doctorName").value("Ana Gómez"));
    }

    // ---------- CRUD y validaciones ----------

    @Test
    void licenciaYDocumentoDuplicados() throws Exception {
        postJson("/api/doctor", """
                {"license":"MED-100","fullName":"Otro","specialty":"Pediatría","consultationFee":80000}
                """).andExpect(status().isConflict());
        postJson("/api/patient", """
                {"document":"P-1","fullName":"Otro","birthDate":"2000-01-01","insuranceType":"NONE"}
                """).andExpect(status().isConflict());
    }

    @Test
    void actualizarMedicoYPaciente() throws Exception {
        mockMvc.perform(put("/api/doctor/MED-100").contentType(MediaType.APPLICATION_JSON).content("""
                {"fullName":"Ana Gómez Ruiz","specialty":"Cardiología","consultationFee":120000}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.specialty").value("Cardiología"))
                .andExpect(jsonPath("$.license").value("MED-100"));
        mockMvc.perform(put("/api/patient/P-1").contentType(MediaType.APPLICATION_JSON).content("""
                {"fullName":"Paciente Uno","birthDate":"1991-01-01","insuranceType":"FULL"}
                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.insuranceType").value("FULL"));
    }

    @Test
    void eliminarMedicoConCitasProgramadasSeRechaza() throws Exception {
        schedule("MED-100", "P-1", MONDAY + "T09:00:00");
        mockMvc.perform(delete("/api/doctor/MED-100")).andExpect(status().isUnprocessableContent());

        mockMvc.perform(patch("/api/appointment/CITA-0001/attend"));
        mockMvc.perform(delete("/api/doctor/MED-100")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/doctor/MED-100")).andExpect(status().isNotFound());
    }

    @Test
    void eliminarPaciente() throws Exception {
        mockMvc.perform(delete("/api/patient/P-3")).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/patient")).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void validacionesDeEntrada() throws Exception {
        postJson("/api/doctor", """
                {"license":"","fullName":"Sin licencia","specialty":"X","consultationFee":-5}
                """)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", startsWith("consultationFee")));
        postJson("/api/patient", """
                {"document":"P-9","fullName":"X","birthDate":"1990-01-01","insuranceType":"PREMIUM"}
                """).andExpect(status().isBadRequest());
    }
}
