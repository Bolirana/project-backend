package com.bolirana.backend.repository;

import com.bolirana.backend.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca un usuario por su correo electrónico.
     *
     * @param correo correo electrónico del usuario
     * @return Optional con el usuario si existe, vacío si no se encuentra
     */
    Optional<Usuario> findByCorreo(String correo);
}
