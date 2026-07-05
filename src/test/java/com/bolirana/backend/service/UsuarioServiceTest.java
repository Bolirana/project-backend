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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("buscarPorId retorna el usuario cuando existe en el repositorio")
    void buscarPorId_usuarioExistente_retornaUsuario() {

        Long id = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(id);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));

        Optional<Usuario> resultado = usuarioService.buscarPorId(id);


        assertThat(resultado).isPresent();
        assertThat(resultado.get()).isEqualTo(usuario);
        verify(usuarioRepository).findById(id);
    }

    @Test
    @DisplayName("buscarPorId retorna Optional vacio cuando el usuario no existe")
    void buscarPorId_usuarioInexistente_retornaOptionalVacio() {

        Long id = 999L;

        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.buscarPorId(id);


        assertThat(resultado).isEmpty();
        verify(usuarioRepository).findById(id);
    }

    @Test
    @DisplayName("crear guarda el usuario de forma correcta")
    void crear_usuarioValido_guardaYRetornaUsuario() {

        Usuario nuevoUsuario = new Usuario();
        
        when(usuarioRepository.save(nuevoUsuario)).thenReturn(nuevoUsuario);

        Usuario resultado = usuarioService.crear(nuevoUsuario);


        assertThat(resultado).isNotNull();
        assertThat(resultado).isEqualTo(nuevoUsuario);
        verify(usuarioRepository).save(nuevoUsuario);
    }

    @Test
    @DisplayName("registrar guarda el usuario con la contrasena hasheada, rol Apostador y estado Activo")
    void registrar_datosValidos_guardaUsuarioConHashRolYEstado() {

        RegistroUsuarioRequest request = new RegistroUsuarioRequest(
                "Ana Torres", "ana@correo.com", "clave123", LocalDate.of(2000, 1, 1));

        when(usuarioRepository.findByCorreo(request.getCorreo())).thenReturn(Optional.empty());
        when(passwordEncoder.encode("clave123")).thenReturn("hash-generado");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        Usuario resultado = usuarioService.registrar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertThat(resultado.getContrasenaHash()).isEqualTo("hash-generado");
        assertThat(guardado.getContrasenaHash()).isEqualTo("hash-generado");
        assertThat(guardado.getRol()).isEqualTo(RolUsuario.APOSTADOR);
        assertThat(guardado.getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    @DisplayName("registrar lanza CorreoDuplicadoException si el correo ya existe")
    void registrar_correoYaRegistrado_lanzaExcepcion() {

        RegistroUsuarioRequest request = new RegistroUsuarioRequest(
                "Ana Torres", "ana@correo.com", "clave123", LocalDate.of(2000, 1, 1));

        when(usuarioRepository.findByCorreo(request.getCorreo())).thenReturn(Optional.of(new Usuario()));

        assertThatThrownBy(() -> usuarioService.registrar(request))
                .isInstanceOf(CorreoDuplicadoException.class);

        verify(usuarioRepository, never()).save(any(Usuario.class));
    }

    @Test
    @DisplayName("registrar lanza IllegalArgumentException si la contrasena es muy corta")
    void registrar_contrasenaCorta_lanzaExcepcion() {

        RegistroUsuarioRequest request = new RegistroUsuarioRequest(
                "Ana Torres", "ana@correo.com", "123", LocalDate.of(2000, 1, 1));

        assertThatThrownBy(() -> usuarioService.registrar(request))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("login retorna el usuario cuando las credenciales son correctas y la cuenta esta activa")
    void login_credencialesValidasYCuentaActiva_retornaUsuario() {

        Usuario usuario = new Usuario();
        usuario.setCorreo("ana@correo.com");
        usuario.setContrasenaHash("hash-generado");
        usuario.setEstado(EstadoUsuario.ACTIVO);

        LoginRequest request = new LoginRequest("ana@correo.com", "clave123");

        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave123", "hash-generado")).thenReturn(true);

        Usuario resultado = usuarioService.login(request);

        assertThat(resultado).isEqualTo(usuario);
    }

    @Test
    @DisplayName("login lanza CredencialesInvalidasException si el correo no existe")
    void login_correoInexistente_lanzaExcepcion() {

        LoginRequest request = new LoginRequest("noexiste@correo.com", "clave123");

        when(usuarioRepository.findByCorreo("noexiste@correo.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.login(request))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("login lanza CredencialesInvalidasException si la contrasena no coincide")
    void login_contrasenaIncorrecta_lanzaExcepcion() {

        Usuario usuario = new Usuario();
        usuario.setCorreo("ana@correo.com");
        usuario.setContrasenaHash("hash-generado");
        usuario.setEstado(EstadoUsuario.ACTIVO);

        LoginRequest request = new LoginRequest("ana@correo.com", "clave-incorrecta");

        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave-incorrecta", "hash-generado")).thenReturn(false);

        assertThatThrownBy(() -> usuarioService.login(request))
                .isInstanceOf(CredencialesInvalidasException.class);
    }

    @Test
    @DisplayName("login lanza CuentaSuspendidaException si la cuenta esta suspendida")
    void login_cuentaSuspendida_lanzaExcepcion() {

        Usuario usuario = new Usuario();
        usuario.setCorreo("ana@correo.com");
        usuario.setContrasenaHash("hash-generado");
        usuario.setEstado(EstadoUsuario.SUSPENDIDO);

        LoginRequest request = new LoginRequest("ana@correo.com", "clave123");

        when(usuarioRepository.findByCorreo("ana@correo.com")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("clave123", "hash-generado")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.login(request))
                .isInstanceOf(CuentaSuspendidaException.class);
    }

    @Test
    @DisplayName("activar cambia el estado del usuario a ACTIVO")
    void activar_usuarioExistente_actualizaEstadoActivo() {

        Long id = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEstado(EstadoUsuario.SUSPENDIDO);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Optional<Usuario> resultado = usuarioService.activar(id);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    @DisplayName("suspender cambia el estado del usuario a SUSPENDIDO")
    void suspender_usuarioExistente_actualizaEstadoSuspendido() {

        Long id = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Optional<Usuario> resultado = usuarioService.suspender(id);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado()).isEqualTo(EstadoUsuario.SUSPENDIDO);
    }

    @Test
    @DisplayName("eliminar cambia el estado del usuario a ELIMINADO sin borrar el registro")
    void eliminar_usuarioExistente_actualizaEstadoEliminado() {

        Long id = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioRepository.findById(id)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        Optional<Usuario> resultado = usuarioService.eliminar(id);

        assertThat(resultado).isPresent();
        assertThat(resultado.get().getEstado()).isEqualTo(EstadoUsuario.ELIMINADO);
    }

    @Test
    @DisplayName("suspender retorna Optional vacio si el usuario no existe")
    void suspender_usuarioInexistente_retornaOptionalVacio() {

        Long id = 999L;

        when(usuarioRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Usuario> resultado = usuarioService.suspender(id);

        assertThat(resultado).isEmpty();
    }
}
