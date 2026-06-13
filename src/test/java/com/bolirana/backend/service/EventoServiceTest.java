package com.bolirana.backend.service;

import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.repository.EventoRepository;
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
class EventoServiceTest {

    @Mock
    private EventoRepository eventoRepository;

    @InjectMocks
    private EventoService eventoService;

    @Test
    @DisplayName("buscarPorId retorna el evento cuando existe en el repositorio")
    void buscarPorId_eventoExistente_retornaEvento() {
        // Caso límite: el id solicitado corresponde a un evento presente en el repositorio
        Long id = 1L;
        Evento evento = new Evento();
        evento.setId(id);
        evento.setNombre("Final Copa America");
        evento.setDeporte("Futbol");
        evento.setEstado("ABIERTO");

        when(eventoRepository.findById(id)).thenReturn(Optional.of(evento));

        Optional<Evento> resultado = eventoService.buscarPorId(id);

        assertThat(resultado).isPresent();
        assertThat(resultado.get()).isEqualTo(evento);
        verify(eventoRepository).findById(id);
    }

    @Test
    @DisplayName("buscarPorId retorna Optional vacío cuando el evento no existe")
    void buscarPorId_eventoInexistente_retornaOptionalVacio() {
        // Caso límite: el id no corresponde a ningún evento, por lo que el controller debe responder 404
        Long id = 999L;

        when(eventoRepository.findById(id)).thenReturn(Optional.empty());

        Optional<Evento> resultado = eventoService.buscarPorId(id);

        assertThat(resultado).isEmpty();
        verify(eventoRepository).findById(id);
    }
}
