package com.bolirana.backend.service;

import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    /** Retorna la lista de todos los usuarios registrados en el sistema. */
    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    /**
     * Busca y retorna un usuario por su identificador único.
     *
     * @param id identificador del usuario
     * @return Optional con el usuario si existe, vacío si no se encuentra
     */
    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }

    /**
     * Busca y retorna un usuario por su correo electrónico.
     *
     * @param correo correo electrónico del usuario
     * @return Optional con el usuario si existe, vacío si no se encuentra
     */
    public Optional<Usuario> buscarPorCorreo(String correo) {
        return usuarioRepository.findByCorreo(correo);
    }

    /**
     * Crea y persiste un nuevo usuario.
     *
     * @param usuario datos del usuario a crear
     * @return el usuario creado y persistido
     */
    public Usuario crear(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }
}
