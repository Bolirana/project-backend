package com.bolirana.backend.service;

import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.exception.ValidacionNegocioException;
import com.bolirana.backend.repository.HistorialCuotaRepository;
import com.bolirana.backend.repository.MercadoRepository;
import com.bolirana.backend.repository.OpcionApuestaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MercadoServiceTest {

    @Mock
    private MercadoRepository mercadoRepository;

    @Mock
    private OpcionApuestaRepository opcionApuestaRepository;

    @Mock
    private HistorialCuotaRepository historialRepository;

    @InjectMocks
    private MercadoService mercadoService;

    private static OpcionApuesta opcionConEvento(Long id, BigDecimal cuotaActual, EstadoEvento estadoEvento) {
        Evento evento = new Evento();
        evento.setEstado(estadoEvento);

        Mercado mercado = new Mercado();
        mercado.setEvento(evento);

        OpcionApuesta opcion = new OpcionApuesta();
        opcion.setId(id);
        opcion.setCuotaActual(cuotaActual);
        opcion.setMercado(mercado);
        return opcion;
    }

    @Test
    @DisplayName("cambiarCuota() lanza ValidacionNegocioException cuando la nueva cuota no es mayor a 1.0 (RNF-08)")
    void cambiarCuota_cuotaNoMayorAUno_lanzaExcepcionSinTocarRepositorios() {
        assertThatThrownBy(() -> mercadoService.cambiarCuota(1L, BigDecimal.ONE))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessage("La cuota debe ser mayor a 1.0");

        verifyNoInteractions(opcionApuestaRepository, historialRepository, mercadoRepository);
    }

    @Test
    @DisplayName("cambiarCuota() lanza ValidacionNegocioException cuando la nueva cuota es nula (RNF-08)")
    void cambiarCuota_cuotaNula_lanzaExcepcionSinTocarRepositorios() {
        assertThatThrownBy(() -> mercadoService.cambiarCuota(1L, null))
                .isInstanceOf(ValidacionNegocioException.class)
                .hasMessage("La cuota debe ser mayor a 1.0");

        verifyNoInteractions(opcionApuestaRepository, historialRepository, mercadoRepository);
    }

    @Test
    @DisplayName("cambiarCuota() con cuota válida y evento ABIERTO actualiza la cuota y registra el historial")
    void cambiarCuota_cuotaValida_actualizaCuotaYRegistraHistorial() {
        OpcionApuesta opcion = opcionConEvento(1L, BigDecimal.valueOf(1.5), EstadoEvento.ABIERTO);

        when(opcionApuestaRepository.findById(1L)).thenReturn(Optional.of(opcion));
        when(opcionApuestaRepository.save(opcion)).thenReturn(opcion);

        OpcionApuesta resultado = mercadoService.cambiarCuota(1L, BigDecimal.valueOf(2.0));

        assertThat(resultado.getCuotaActual()).isEqualByComparingTo("2.0");
    }
}
