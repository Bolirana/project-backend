package com.bolirana.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Provee el algoritmo de hashing de contrasenas usado en todo el sistema.
 * No se usa spring-boot-starter-security completo a proposito: eso activaria
 * un filtro de autenticacion que bloquearia los endpoints publicos existentes.
 * El control de acceso por rol (quien puede llamar a los endpoints de
 * administrador) queda pendiente para una siguiente iteracion con sesiones o JWT.
 */
@Configuration
public class SecurityConfig {

    /**
     * Bean del codificador de contrasenas usado para el registro y el login.
     *
     * @return una instancia de BCryptPasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
