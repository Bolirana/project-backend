package com.bolirana.backend.service;

import com.bolirana.backend.domain.EstadoUsuario;
import com.bolirana.backend.domain.RolUsuario;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.dto.LoginRequest;
import com.bolirana.backend.dto.RegistroUsuarioRequest;
import com.bolirana.backend.exception.CorreoDuplicadoException;
import com.bolirana.backend.exception.CredencialesInvalidasException;
import com.bolirana.backend.exception.CuentaSuspendidaException;
import com.bolirana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

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

    /**
     * Registra un nuevo usuario en el sistema con rol de Apostador y cuenta activa.
     * La contrasena se hashea antes de persistirse; nunca se guarda en texto plano.
     *
     * @param request datos de registro (nombre, correo, contrasena en texto plano, fecha de nacimiento)
     * @return el usuario creado y persistido
     * @throws CorreoDuplicadoException si el correo ya esta registrado
     * @throws IllegalArgumentException si algun campo obligatorio falta o es invalido
     */
    public Usuario registrar(RegistroUsuarioRequest request) {
        validarDatosRegistro(request);

        if (usuarioRepository.findByCorreo(request.getCorreo()).isPresent()) {
            throw new CorreoDuplicadoException(request.getCorreo());
        }

        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(request.getNombreCompleto());
        usuario.setCorreo(request.getCorreo());
        usuario.setContrasenaHash(passwordEncoder.encode(request.getContrasena()));
        usuario.setFechaNacimiento(request.getFechaNacimiento());
        usuario.setRol(RolUsuario.APOSTADOR);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        return usuarioRepository.save(usuario);
    }

    /**
     * Valida los campos obligatorios de una solicitud de registro.
     *
     * @param request datos de registro a validar
     * @throws IllegalArgumentException si algun campo obligatorio falta o es invalido
     */
    private void validarDatosRegistro(RegistroUsuarioRequest request) {
        if (request.getNombreCompleto() == null || request.getNombreCompleto().isBlank()) {
            throw new IllegalArgumentException("El nombre completo es obligatorio");
        }
        if (request.getCorreo() == null || !request.getCorreo().contains("@")) {
            throw new IllegalArgumentException("El correo electronico no es valido");
        }
        if (request.getContrasena() == null || request.getContrasena().length() < 6) {
            throw new IllegalArgumentException("La contrasena debe tener al menos 6 caracteres");
        }
        if (request.getFechaNacimiento() == null) {
            throw new IllegalArgumentException("La fecha de nacimiento es obligatoria");
        }
        if (Period.between(request.getFechaNacimiento(), LocalDate.now()).getYears() < 18) {
            throw new IllegalArgumentException("Debes ser mayor de 18 años para registrarte");
        }
    }

    /**
     * Autentica a un usuario por correo y contrasena. Bloquea el acceso si la
     * cuenta esta suspendida o eliminada.
     *
     * @param request credenciales de acceso (correo y contrasena en texto plano)
     * @return el usuario autenticado, incluyendo su rol
     * @throws CredencialesInvalidasException si el correo no existe o la contrasena no coincide
     * @throws CuentaSuspendidaException si la cuenta esta suspendida o eliminada
     */
    public Usuario login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByCorreo(request.getCorreo())
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(request.getContrasena(), usuario.getContrasenaHash())) {
            throw new CredencialesInvalidasException();
        }

        if (usuario.getEstado() == EstadoUsuario.SUSPENDIDO || usuario.getEstado() == EstadoUsuario.ELIMINADO) {
            throw new CuentaSuspendidaException();
        }

        return usuario;
    }

    /**
     * Activa la cuenta de un usuario, permitiendole iniciar sesion nuevamente.
     *
     * @param id identificador del usuario
     * @return Optional con el usuario actualizado, vacio si no existe
     */
    public Optional<Usuario> activar(Long id) {
        return cambiarEstado(id, EstadoUsuario.ACTIVO);
    }

    /**
     * Suspende la cuenta de un usuario, impidiendole iniciar sesion.
     *
     * @param id identificador del usuario
     * @return Optional con el usuario actualizado, vacio si no existe
     */
    public Optional<Usuario> suspender(Long id) {
        return cambiarEstado(id, EstadoUsuario.SUSPENDIDO);
    }

    /**
     * Elimina (logicamente) la cuenta de un usuario. Se conserva el registro en base
     * de datos, ya que Apuesta y MovimientoSaldo mantienen una referencia hacia el
     * usuario; un borrado fisico rompería esa integridad referencial e historica.
     *
     * @param id identificador del usuario
     * @return Optional con el usuario actualizado, vacio si no existe
     */
    public Optional<Usuario> eliminar(Long id) {
        return cambiarEstado(id, EstadoUsuario.ELIMINADO);
    }

    /**
     * Cambia el estado de la cuenta de un usuario y persiste el cambio.
     *
     * @param id identificador del usuario
     * @param nuevoEstado estado a asignar
     * @return Optional con el usuario actualizado, vacio si no existe
     */
    private Optional<Usuario> cambiarEstado(Long id, EstadoUsuario nuevoEstado) {
        return usuarioRepository.findById(id)
                .map(usuario -> {
                    usuario.setEstado(nuevoEstado);
                    return usuarioRepository.save(usuario);
                });
    }
}
