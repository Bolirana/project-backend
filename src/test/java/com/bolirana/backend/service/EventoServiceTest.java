package com.bolirana.backend.service;

import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.dto.EventoCreacionDTO;
import com.bolirana.backend.dto.MercadoDTO;
import com.bolirana.backend.dto.OpcionApuestaDTO;
import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.exception.RecursoNoEncontradoException;
import com.bolirana.backend.exception.ValidacionNegocioException;
import com.bolirana.backend.repository.EventoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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
        evento.setEstado(EstadoEvento.ABIERTO);

        when(eventoRepository.findById(id)).thenReturn(Optional.of(evento));

        Evento resultado = eventoService.obtenerEventoPorId(id);

        assertThat(resultado).isEqualTo(evento);
        verify(eventoRepository).findById(id);
    }

    @Test
    @DisplayName("buscarPorId lanza RecursoNoEncontradoException cuando el evento no existe")
    void buscarPorId_eventoInexistente_lanzaExcepcion() {
        // Caso límite: el id no corresponde a ningún evento, por lo que el controller debe responder 404
        Long id = 999L;

        when(eventoRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventoService.obtenerEventoPorId(id))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(eventoRepository).findById(id);
    }

    private static EventoCreacionDTO dtoConFechaYCuota(LocalDate fechaEvento, BigDecimal cuotaActual) {
        OpcionApuestaDTO opcion = new OpcionApuestaDTO();
        opcion.setNombre("Local gana");
        opcion.setCuotaActual(cuotaActual);

        MercadoDTO mercado = new MercadoDTO();
        mercado.setNombre("Resultado del partido");
        mercado.setOpciones(List.of(opcion));

        EventoCreacionDTO dto = new EventoCreacionDTO();
        dto.setEquipoLocal("Colombia");
        dto.setEquipoVisitante("Brasil");
        dto.setDeporte("Futbol");
        dto.setFechaEvento(fechaEvento);
        dto.setMercados(List.of(mercado));
        return dto;
    }

    @Test
    @DisplayName("crearEvento() con datos válidos construye y persiste el evento con sus mercados y opciones")
    void crearEvento_datosValidos_creaEventoConMercadosYOpciones() {
        EventoCreacionDTO dto = dtoConFechaYCuota(LocalDate.now().plusDays(1), BigDecimal.valueOf(2.0));

        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Evento resultado = eventoService.crearEvento(dto);

        assertThat(resultado.getNombre()).isEqualTo("Colombia vs Brasil");
        assertThat(resultado.getMercados()).hasSize(1);
        assertThat(resultado.getMercados().get(0).getOpciones()).hasSize(1);
        assertThat(resultado.getMercados().get(0).getOpciones().get(0).getCuotaActual())
                .isEqualByComparingTo("2.0");
    }

    @Test
    @DisplayName("crearEvento() lanza ValidacionNegocioException cuando la fecha es anterior a hoy (RNF-08)")
    void crearEvento_fechaAnteriorAHoy_lanzaExcepcion() {
        EventoCreacionDTO dto = dtoConFechaYCuota(LocalDate.now().minusDays(1), BigDecimal.valueOf(2.0));

        assertThatThrownBy(() -> eventoService.crearEvento(dto))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessage("La fecha del evento no puede ser anterior a la fecha actual");

        verify(eventoRepository, never()).save(any(Evento.class));
    }

    @Test
    @DisplayName("crearEvento() con fecha de hoy no lanza excepción (RNF-08 permite la fecha actual)")
    void crearEvento_fechaHoy_noLanzaExcepcion() {
        EventoCreacionDTO dto = dtoConFechaYCuota(LocalDate.now(), BigDecimal.valueOf(2.0));

        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        assertThat(eventoService.crearEvento(dto)).isNotNull();
    }

    @Test
    @DisplayName("crearEvento() lanza ValidacionNegocioException cuando una cuota no es mayor a 1.0 (RNF-08)")
    void crearEvento_cuotaNoMayorAUno_lanzaExcepcion() {
        EventoCreacionDTO dto = dtoConFechaYCuota(LocalDate.now().plusDays(1), BigDecimal.valueOf(1.0));

        assertThatThrownBy(() -> eventoService.crearEvento(dto))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessage("La cuota debe ser mayor a 1.0");

        verify(eventoRepository, never()).save(any(Evento.class));
    }
}
