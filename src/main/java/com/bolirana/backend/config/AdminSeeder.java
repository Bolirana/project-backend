package com.bolirana.backend.config;

import com.bolirana.backend.domain.EstadoUsuario;
import com.bolirana.backend.domain.RolUsuario;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

/**
 * Crea un usuario ADMINISTRADOR por defecto al arrancar la aplicacion, ya que
 * ningun endpoint publico permite crear o promover un usuario a ese rol
 * (registro fuerza APOSTADOR). Es idempotente: no hace nada si el correo ya existe.
 * Solo para desarrollo/pruebas locales; no pensado para un despliegue real.
 */
@Component
@RequiredArgsConstructor
public class AdminSeeder implements CommandLineRunner {

    private static final String CORREO_ADMIN = "admin@win24.com";
    private static final String CONTRASENA_ADMIN = "admin123";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (usuarioRepository.findByCorreo(CORREO_ADMIN).isPresent()) {
            return;
        }

        Usuario admin = new Usuario();
        admin.setNombreCompleto("Administrador WIN24");
        admin.setCorreo(CORREO_ADMIN);
        admin.setContrasenaHash(passwordEncoder.encode(CONTRASENA_ADMIN));
        admin.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        admin.setRol(RolUsuario.ADMINISTRADOR);
        admin.setEstado(EstadoUsuario.ACTIVO);

        usuarioRepository.save(admin);
    }
}
