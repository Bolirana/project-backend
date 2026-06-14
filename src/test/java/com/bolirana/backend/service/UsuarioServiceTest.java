package com.bolirana.backend.service;

import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

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
}