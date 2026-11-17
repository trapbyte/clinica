package com.example.clinica.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClockConfig {

    // Reloj inyectable: las reglas que dependen de "ahora" (RN3, RN8) se pueden probar con una fecha fija
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
