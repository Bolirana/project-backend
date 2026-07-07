package com.bolirana.backend.service;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.ConfiguracionRiesgo;
import com.bolirana.backend.domain.EstadoApuesta;
import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.dto.ExposicionEventoDTO;
import com.bolirana.backend.dto.ExposicionOpcionDTO;
import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.exception.RecursoNoEncontradoException;
import com.bolirana.backend.repository.ApuestaRepository;
import com.bolirana.backend.repository.ConfiguracionRiesgoRepository;
import com.bolirana.backend.repository.EventoRepository;
import com.bolirana.backend.repository.MercadoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * EventoService y ConfiguracionRiesgoService no pueden mockearse con @Mock en este JDK
 * (byte-buddy no logra instrumentar clases concretas), así que se construyen instancias
 * reales respaldadas por repositorios mockeados, siguiendo el mismo patrón de ApuestaServiceTest.
 */
@ExtendWith(MockitoExtension.class)
class MotorRiesgoServiceTest {

    @Mock
    private EventoRepository eventoRepository;
    @Mock
    private MercadoRepository mercadoRepository;
    @Mock
    private ApuestaRepository apuestaRepository;
    @Mock
    private ConfiguracionRiesgoRepository configuracionRiesgoRepository;

    private MotorRiesgoService motorRiesgoService;

    @BeforeEach
    void setUp() {
        EventoService eventoService = new EventoService(eventoRepository);
        ConfiguracionRiesgoService configuracionRiesgoService =
                new ConfiguracionRiesgoService(configuracionRiesgoRepository);
        motorRiesgoService =
                new MotorRiesgoService(eventoService, mercadoRepository, apuestaRepository, configuracionRiesgoService);
    }

    private Evento eventoAbierto(Long id, String nombre) {
        Evento evento = new Evento();
        evento.setId(id);
        evento.setNombre(nombre);
        evento.setEstado(EstadoEvento.ABIERTO);
        return evento;
    }

    private Mercado mercado(Long id, String nombre, Evento evento) {
        Mercado mercado = new Mercado();
        mercado.setId(id);
        mercado.setNombre(nombre);
        mercado.setEvento(evento);
        return mercado;
    }

    private OpcionApuesta opcion(Long id, String nombre, BigDecimal cuota, Mercado mercado) {
        OpcionApuesta opcion = new OpcionApuesta();
        opcion.setId(id);
        opcion.setNombre(nombre);
        opcion.setCuotaActual(cuota);
        opcion.setMercado(mercado);
        mercado.getOpciones().add(opcion);
        return opcion;
    }

    private Apuesta apuestaRegistrada(OpcionApuesta opcion, double monto, double cuotaCongelada) {
        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setMonto(monto);
        apuesta.setCuotaCongelada(cuotaCongelada);
        apuesta.setEstado(EstadoApuesta.REGISTRADA);
        return apuesta;
    }

    @Test
    @DisplayName("calcularExposicion() retorna exposición 0 y sin alerta cuando la opción no tiene apuestas")
    void calcularExposicion_sinApuestas_exposicionCeroSinAlerta() {
        Evento evento = eventoAbierto(1L, "Colombia vs Brasil");
        Mercado mercado = mercado(1L, "Resultado del partido", evento);
        opcion(1L, "Colombia gana", new BigDecimal("2.50"), mercado);

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(mercadoRepository.findByEventoId(1L)).thenReturn(List.of(mercado));
        when(apuestaRepository.findByOpcionMercadoEventoIdAndEstado(1L, EstadoApuesta.REGISTRADA))
                .thenReturn(List.of());
        when(configuracionRiesgoRepository.findByMercadoId(1L)).thenReturn(Optional.empty());

        ExposicionEventoDTO resultado = motorRiesgoService.calcularExposicion(1L);

        assertThat(resultado.mercados()).hasSize(1);
        ExposicionOpcionDTO opcionDTO = resultado.mercados().get(0).opciones().get(0);
        assertThat(opcionDTO.exposicion()).isZero();
        assertThat(opcionDTO.alerta()).isFalse();
        assertThat(opcionDTO.sugerencia()).isNull();
    }

    @Test
    @DisplayName("calcularExposicion() no genera alerta cuando la exposición está bajo el límite configurado")
    void calcularExposicion_bajoLimite_noGeneraAlerta() {
        Evento evento = eventoAbierto(1L, "Colombia vs Brasil");
        Mercado mercado = mercado(1L, "Resultado del partido", evento);
        OpcionApuesta opcion = opcion(1L, "Colombia gana", new BigDecimal("2.50"), mercado);

        ConfiguracionRiesgo config = new ConfiguracionRiesgo();
        config.setMercado(mercado);
        config.setLimiteAlerta(500_000.0);

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(mercadoRepository.findByEventoId(1L)).thenReturn(List.of(mercado));
        when(apuestaRepository.findByOpcionMercadoEventoIdAndEstado(1L, EstadoApuesta.REGISTRADA))
                .thenReturn(List.of(apuestaRegistrada(opcion, 50_000.0, 2.5)));
        when(configuracionRiesgoRepository.findByMercadoId(1L)).thenReturn(Optional.of(config));

        ExposicionEventoDTO resultado = motorRiesgoService.calcularExposicion(1L);

        ExposicionOpcionDTO opcionDTO = resultado.mercados().get(0).opciones().get(0);
        assertThat(opcionDTO.exposicion()).isEqualTo(125_000.0);
        assertThat(opcionDTO.alerta()).isFalse();
        assertThat(opcionDTO.sugerencia()).isNull();
    }

    @Test
    @DisplayName("calcularExposicion() genera alerta y sugiere una cuota menor cuando se supera el límite configurado")
    void calcularExposicion_sobreLimite_generaAlertaYSugerenciaCuota() {
        Evento evento = eventoAbierto(1L, "Colombia vs Brasil");
        Mercado mercado = mercado(1L, "Resultado del partido", evento);
        OpcionApuesta opcion = opcion(1L, "Colombia gana", new BigDecimal("2.50"), mercado);

        ConfiguracionRiesgo config = new ConfiguracionRiesgo();
        config.setMercado(mercado);
        config.setLimiteAlerta(500_000.0);

        // Dos apuestas de 150.000 a cuota 2.5 => exposición 750.000, por encima del límite de 500.000
        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(mercadoRepository.findByEventoId(1L)).thenReturn(List.of(mercado));
        when(apuestaRepository.findByOpcionMercadoEventoIdAndEstado(1L, EstadoApuesta.REGISTRADA))
                .thenReturn(List.of(
                        apuestaRegistrada(opcion, 150_000.0, 2.5),
                        apuestaRegistrada(opcion, 150_000.0, 2.5)));
        when(configuracionRiesgoRepository.findByMercadoId(1L)).thenReturn(Optional.of(config));

        ExposicionEventoDTO resultado = motorRiesgoService.calcularExposicion(1L);

        ExposicionOpcionDTO opcionDTO = resultado.mercados().get(0).opciones().get(0);
        assertThat(opcionDTO.exposicion()).isEqualTo(750_000.0);
        assertThat(opcionDTO.alerta()).isTrue();
        assertThat(opcionDTO.sugerencia()).isNotNull();
        assertThat(opcionDTO.sugerencia().cuotaActual()).isEqualByComparingTo("2.50");
        // cuotaSugerida = 2.50 * (500.000 / 750.000) ≈ 1.67
        assertThat(opcionDTO.sugerencia().cuotaSugerida()).isEqualByComparingTo("1.67");
        assertThat(opcionDTO.sugerencia().razon()).isNotBlank();
    }

    @Test
    @DisplayName("calcularExposicion() usa el límite por defecto de 500.000 cuando el mercado no tiene ConfiguracionRiesgo")
    void calcularExposicion_sinConfiguracion_usaLimitePorDefecto() {
        Evento evento = eventoAbierto(1L, "Colombia vs Brasil");
        Mercado mercado = mercado(1L, "Resultado del partido", evento);
        OpcionApuesta opcion = opcion(1L, "Colombia gana", new BigDecimal("2.50"), mercado);

        when(eventoRepository.findById(1L)).thenReturn(Optional.of(evento));
        when(mercadoRepository.findByEventoId(1L)).thenReturn(List.of(mercado));
        when(apuestaRepository.findByOpcionMercadoEventoIdAndEstado(1L, EstadoApuesta.REGISTRADA))
                .thenReturn(List.of(apuestaRegistrada(opcion, 600_000.0, 2.5)));
        when(configuracionRiesgoRepository.findByMercadoId(1L)).thenReturn(Optional.empty());

        ExposicionEventoDTO resultado = motorRiesgoService.calcularExposicion(1L);

        ExposicionOpcionDTO opcionDTO = resultado.mercados().get(0).opciones().get(0);
        assertThat(opcionDTO.limiteAlerta()).isEqualTo(500_000.0);
        assertThat(opcionDTO.alerta()).isTrue();
    }

    @Test
    @DisplayName("calcularExposicion() lanza RecursoNoEncontradoException si el evento no existe")
    void calcularExposicion_eventoInexistente_lanzaExcepcion() {
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> motorRiesgoService.calcularExposicion(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
