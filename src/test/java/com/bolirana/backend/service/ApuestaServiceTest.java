package com.bolirana.backend.service;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.EstadoApuesta;
import com.bolirana.backend.domain.EstadoUsuario;
import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.dto.HistorialApostadorResponse;
import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.exception.TransicionEstadoInvalidaException;
import com.bolirana.backend.repository.ApuestaRepository;
import com.bolirana.backend.repository.EventoRepository;
import com.bolirana.backend.repository.MovimientoSaldoRepository;
import com.bolirana.backend.repository.OpcionApuestaRepository;
import com.bolirana.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApuestaServiceTest {

    @Mock
    private ApuestaRepository apuestaRepository;

    @Mock
    private OpcionApuestaRepository opcionApuestaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private MovimientoSaldoRepository movimientoSaldoRepository;

    @Mock
    private EventoRepository eventoRepository;

    // Instancia real: MovimientoSaldoService y EventoService no son interfaces y en este
    // entorno (JDK 26) Mockito no puede instrumentar clases concretas, así que se colabora
    // con las clases reales apoyadas en sus propios repositorios mockeados en lugar de
    // mockear los servicios.
    private ApuestaService apuestaService;

    @BeforeEach
    void setUp() {
        MovimientoSaldoService movimientoSaldoService =
                new MovimientoSaldoService(movimientoSaldoRepository, usuarioRepository);
        EventoService eventoService = new EventoService(eventoRepository);
        apuestaService = new ApuestaService(
                apuestaRepository, opcionApuestaRepository, usuarioRepository, movimientoSaldoService, eventoService);
    }

    private static OpcionApuesta opcionConEvento(Long id, Double cuotaActual, EstadoEvento estadoEvento) {
        Evento evento = new Evento();
        evento.setEstado(estadoEvento);

        Mercado mercado = new Mercado();
        mercado.setEvento(evento);

        OpcionApuesta opcion = new OpcionApuesta();
        opcion.setId(id);
        opcion.setCuotaActual(BigDecimal.valueOf(cuotaActual));
        opcion.setMercado(mercado);
        return opcion;
    }

    private static Usuario apostadorConSaldo(Long id, Double saldo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setSaldo(saldo);
        usuario.setEstado(EstadoUsuario.ACTIVO);
        return usuario;
    }

    @Test
    @DisplayName("crear() asigna cuotaCongelada igual a cuotaActual de la opción en el momento de registrar")
    void crear_opcionExistente_congelajCuotaActual() {
        // Caso límite: la cuota puede cambiar después; lo que se guarda debe ser el valor vigente al momento de crear
        OpcionApuesta opcion = opcionConEvento(1L, 2.5, EstadoEvento.ABIERTO);
        Usuario apostador = apostadorConSaldo(10L, 50000.0);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setApostador(apostador);
        apuesta.setMonto(10000.0);

        when(opcionApuestaRepository.findById(1L)).thenReturn(Optional.of(opcion));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(apostador));
        when(apuestaRepository.save(apuesta)).thenReturn(apuesta);

        Apuesta resultado = apuestaService.crear(apuesta);

        assertThat(resultado.getCuotaCongelada()).isEqualTo(2.5);
    }

    @Test
    @DisplayName("crear() inicializa el estado de la apuesta en REGISTRADA")
    void crear_opcionExistente_inicializaEstadoRegistrada() {
        // Caso límite: el estado inicial siempre debe ser REGISTRADA sin importar lo que el cliente envíe
        OpcionApuesta opcion = opcionConEvento(2L, 1.8, EstadoEvento.ABIERTO);
        Usuario apostador = apostadorConSaldo(11L, 20000.0);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setApostador(apostador);
        apuesta.setMonto(5000.0);

        when(opcionApuestaRepository.findById(2L)).thenReturn(Optional.of(opcion));
        when(usuarioRepository.findById(11L)).thenReturn(Optional.of(apostador));
        when(apuestaRepository.save(apuesta)).thenReturn(apuesta);

        Apuesta resultado = apuestaService.crear(apuesta);

        assertThat(resultado.getEstado()).isEqualTo(EstadoApuesta.REGISTRADA);
    }

    @Test
    @DisplayName("crear() lanza IllegalArgumentException y no llama a save() cuando la opción no existe")
    void crear_opcionInexistente_lanzaExcepcionSinGuardar() {
        // Caso límite: el id de opción no existe en la base de datos, la apuesta no debe persistirse
        OpcionApuesta opcionInexistente = new OpcionApuesta();
        opcionInexistente.setId(999L);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcionInexistente);
        apuesta.setMonto(10000.0);

        when(opcionApuestaRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apuestaService.crear(apuesta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Opcion de apuesta no encontrada");

        verify(apuestaRepository, never()).save(apuesta);
    }

    @Test
    @DisplayName("crear() lanza IllegalArgumentException y no llama a save() cuando el evento no está ABIERTO")
    void crear_eventoNoAbierto_lanzaExcepcionSinGuardar() {
        // Caso límite: aunque la opción exista, no se debe permitir apostar si el evento ya cerró
        OpcionApuesta opcion = opcionConEvento(3L, 2.0, EstadoEvento.CERRADO);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setMonto(10000.0);

        when(opcionApuestaRepository.findById(3L)).thenReturn(Optional.of(opcion));

        assertThatThrownBy(() -> apuestaService.crear(apuesta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("No se puede apostar: el evento no está ABIERTO");

        verify(apuestaRepository, never()).save(apuesta);
    }

    @Test
    @DisplayName("crear() lanza IllegalArgumentException y no llama a save() cuando el apostador no existe")
    void crear_apostadorInexistente_lanzaExcepcionSinGuardar() {
        OpcionApuesta opcion = opcionConEvento(4L, 2.0, EstadoEvento.ABIERTO);
        Usuario apostadorInexistente = new Usuario();
        apostadorInexistente.setId(404L);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setApostador(apostadorInexistente);
        apuesta.setMonto(10000.0);

        when(opcionApuestaRepository.findById(4L)).thenReturn(Optional.of(opcion));
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apuestaService.crear(apuesta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuario apostador no encontrado");

        verify(apuestaRepository, never()).save(apuesta);
        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
    }

    @Test
    @DisplayName("crear() lanza IllegalArgumentException y no llama a save() cuando el apostador no está ACTIVO")
    void crear_apostadorNoActivo_lanzaExcepcionSinGuardar() {
        OpcionApuesta opcion = opcionConEvento(15L, 2.0, EstadoEvento.ABIERTO);
        Usuario apostadorSuspendido = apostadorConSaldo(16L, 50000.0);
        apostadorSuspendido.setEstado(EstadoUsuario.SUSPENDIDO);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setApostador(apostadorSuspendido);
        apuesta.setMonto(10000.0);

        when(opcionApuestaRepository.findById(15L)).thenReturn(Optional.of(opcion));
        when(usuarioRepository.findById(16L)).thenReturn(Optional.of(apostadorSuspendido));

        assertThatThrownBy(() -> apuestaService.crear(apuesta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El usuario no puede realizar apuestas: cuenta suspendida o eliminada");

        verify(apuestaRepository, never()).save(apuesta);
        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
    }

    @Test
    @DisplayName("crear() lanza IllegalArgumentException y no llama a save() cuando el saldo es insuficiente")
    void crear_saldoInsuficiente_lanzaExcepcionSinGuardar() {
        OpcionApuesta opcion = opcionConEvento(5L, 2.0, EstadoEvento.ABIERTO);
        Usuario apostador = apostadorConSaldo(12L, 3000.0);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setApostador(apostador);
        apuesta.setMonto(10000.0);

        when(opcionApuestaRepository.findById(5L)).thenReturn(Optional.of(opcion));
        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(apostador));

        assertThatThrownBy(() -> apuestaService.crear(apuesta))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saldo insuficiente para realizar la apuesta");

        verify(apuestaRepository, never()).save(apuesta);
        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
    }

    @Test
    @DisplayName("crear() con saldo suficiente descuenta el saldo y genera un MovimientoSaldo de tipo APUESTA")
    void crear_saldoSuficiente_descuentaSaldoYGeneraMovimientoTipoApuesta() {
        OpcionApuesta opcion = opcionConEvento(6L, 1.5, EstadoEvento.ABIERTO);
        Usuario apostador = apostadorConSaldo(13L, 20000.0);

        Apuesta apuesta = new Apuesta();
        apuesta.setOpcion(opcion);
        apuesta.setApostador(apostador);
        apuesta.setMonto(8000.0);

        when(opcionApuestaRepository.findById(6L)).thenReturn(Optional.of(opcion));
        when(usuarioRepository.findById(13L)).thenReturn(Optional.of(apostador));
        when(apuestaRepository.save(apuesta)).thenReturn(apuesta);
        when(movimientoSaldoRepository.save(any(MovimientoSaldo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        apuestaService.crear(apuesta);

        assertThat(apostador.getSaldo()).isEqualTo(12000.0);

        ArgumentCaptor<MovimientoSaldo> captor = ArgumentCaptor.forClass(MovimientoSaldo.class);
        verify(movimientoSaldoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("APUESTA");
        assertThat(captor.getValue().getMonto()).isEqualTo(8000.0);
        assertThat(captor.getValue().getUsuario()).isEqualTo(apostador);
    }

    private static Evento eventoConEstado(Long id, EstadoEvento estado) {
        Evento evento = new Evento();
        evento.setId(id);
        evento.setEstado(estado);
        return evento;
    }

    private static Apuesta apuestaConEstado(Long id, EstadoApuesta estado, Usuario apostador, Double monto,
            Double cuotaCongelada) {
        Apuesta apuesta = new Apuesta();
        apuesta.setId(id);
        apuesta.setEstado(estado);
        apuesta.setApostador(apostador);
        apuesta.setMonto(monto);
        apuesta.setCuotaCongelada(cuotaCongelada);
        return apuesta;
    }

    @Test
    @DisplayName("resolver() transiciona una apuesta REGISTRADA a GANADA")
    void resolver_apuestaRegistrada_transicionaAGanada() {
        Apuesta apuesta = apuestaConEstado(1L, EstadoApuesta.REGISTRADA, apostadorConSaldo(1L, 10000.0), 5000.0, 2.0);

        when(apuestaRepository.findById(1L)).thenReturn(Optional.of(apuesta));
        when(apuestaRepository.save(apuesta)).thenReturn(apuesta);

        Apuesta resultado = apuestaService.resolver(1L, EstadoApuesta.GANADA);

        assertThat(resultado.getEstado()).isEqualTo(EstadoApuesta.GANADA);
    }

    @Test
    @DisplayName("resolver() transiciona una apuesta REGISTRADA a PERDIDA")
    void resolver_apuestaRegistrada_transicionaAPerdida() {
        Apuesta apuesta = apuestaConEstado(2L, EstadoApuesta.REGISTRADA, apostadorConSaldo(1L, 10000.0), 5000.0, 2.0);

        when(apuestaRepository.findById(2L)).thenReturn(Optional.of(apuesta));
        when(apuestaRepository.save(apuesta)).thenReturn(apuesta);

        Apuesta resultado = apuestaService.resolver(2L, EstadoApuesta.PERDIDA);

        assertThat(resultado.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
    }

    @Test
    @DisplayName("resolver() lanza IllegalArgumentException cuando el resultado no es GANADA ni PERDIDA")
    void resolver_resultadoInvalido_lanzaExcepcion() {
        assertThatThrownBy(() -> apuestaService.resolver(3L, EstadoApuesta.PAGADA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El resultado debe ser GANADA o PERDIDA");

        verify(apuestaRepository, never()).findById(3L);
    }

    @Test
    @DisplayName("resolver() lanza IllegalArgumentException cuando la apuesta no existe")
    void resolver_apuestaInexistente_lanzaExcepcion() {
        when(apuestaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apuestaService.resolver(99L, EstadoApuesta.GANADA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Apuesta no encontrada");

        verify(apuestaRepository, never()).save(any(Apuesta.class));
    }

    @Test
    @DisplayName("resolver() lanza IllegalArgumentException cuando la apuesta no está en estado REGISTRADA")
    void resolver_apuestaNoRegistrada_lanzaExcepcion() {
        Apuesta apuesta = apuestaConEstado(4L, EstadoApuesta.GANADA, apostadorConSaldo(1L, 10000.0), 5000.0, 2.0);

        when(apuestaRepository.findById(4L)).thenReturn(Optional.of(apuesta));

        assertThatThrownBy(() -> apuestaService.resolver(4L, EstadoApuesta.PERDIDA))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Solo se puede resolver una apuesta en estado REGISTRADA");

        verify(apuestaRepository, never()).save(apuesta);
    }

    @Test
    @DisplayName("pagar() acredita monto * cuotaCongelada al apostador y transiciona la apuesta a PAGADA")
    void pagar_apuestaGanada_acreditaMontoYTransicionaAPagada() {
        Usuario apostador = apostadorConSaldo(1L, 10000.0);
        Apuesta apuesta = apuestaConEstado(5L, EstadoApuesta.GANADA, apostador, 5000.0, 2.5);

        when(apuestaRepository.findById(5L)).thenReturn(Optional.of(apuesta));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(apostador));
        when(movimientoSaldoRepository.save(any(MovimientoSaldo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(apuestaRepository.save(apuesta)).thenReturn(apuesta);

        Apuesta resultado = apuestaService.pagar(5L);

        assertThat(resultado.getEstado()).isEqualTo(EstadoApuesta.PAGADA);
        assertThat(apostador.getSaldo()).isEqualTo(22500.0);

        ArgumentCaptor<MovimientoSaldo> captor = ArgumentCaptor.forClass(MovimientoSaldo.class);
        verify(movimientoSaldoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("PAGO_APUESTA");
        assertThat(captor.getValue().getMonto()).isEqualTo(12500.0);
        assertThat(captor.getValue().getUsuario()).isEqualTo(apostador);
    }

    @Test
    @DisplayName("pagar() lanza IllegalArgumentException cuando la apuesta no existe")
    void pagar_apuestaInexistente_lanzaExcepcion() {
        when(apuestaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apuestaService.pagar(99L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Apuesta no encontrada");

        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
    }

    @Test
    @DisplayName("pagar() lanza IllegalArgumentException cuando la apuesta no está en estado GANADA")
    void pagar_apuestaNoGanada_lanzaExcepcion() {
        Apuesta apuesta = apuestaConEstado(6L, EstadoApuesta.REGISTRADA, apostadorConSaldo(1L, 10000.0), 5000.0, 2.0);

        when(apuestaRepository.findById(6L)).thenReturn(Optional.of(apuesta));

        assertThatThrownBy(() -> apuestaService.pagar(6L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Solo se puede pagar una apuesta en estado GANADA");

        verify(apuestaRepository, never()).save(apuesta);
        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
    }

    @Test
    @DisplayName("liquidarEvento() resuelve GANADA/PERDIDA según la opción ganadora, paga la ganadora "
            + "y transiciona el evento a LIQUIDADO")
    void liquidarEvento_conApuestasRegistradas_resuelveYPagaGanadorasYLiquidaEvento() {
        Usuario apostadorGanador = apostadorConSaldo(20L, 10000.0);
        Usuario apostadorPerdedor = apostadorConSaldo(21L, 10000.0);

        OpcionApuesta opcionGanadora = new OpcionApuesta();
        opcionGanadora.setId(100L);

        OpcionApuesta opcionPerdedora = new OpcionApuesta();
        opcionPerdedora.setId(200L);

        Apuesta apuestaGanadora = apuestaConEstado(1L, EstadoApuesta.REGISTRADA, apostadorGanador, 5000.0, 2.0);
        apuestaGanadora.setOpcion(opcionGanadora);

        Apuesta apuestaPerdedora = apuestaConEstado(2L, EstadoApuesta.REGISTRADA, apostadorPerdedor, 3000.0, 1.5);
        apuestaPerdedora.setOpcion(opcionPerdedora);

        Evento evento = eventoConEstado(50L, EstadoEvento.CERRADO);

        when(apuestaRepository.findByOpcionMercadoEventoIdAndEstado(50L, EstadoApuesta.REGISTRADA))
                .thenReturn(List.of(apuestaGanadora, apuestaPerdedora));
        when(apuestaRepository.findById(1L)).thenReturn(Optional.of(apuestaGanadora));
        when(apuestaRepository.findById(2L)).thenReturn(Optional.of(apuestaPerdedora));
        when(apuestaRepository.save(any(Apuesta.class))).thenAnswer(invocacion -> invocacion.getArgument(0));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(apostadorGanador));
        when(movimientoSaldoRepository.save(any(MovimientoSaldo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));
        when(eventoRepository.findById(50L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        List<Apuesta> resultado = apuestaService.liquidarEvento(50L, 100L);

        assertThat(resultado).hasSize(2);
        assertThat(apuestaGanadora.getEstado()).isEqualTo(EstadoApuesta.PAGADA);
        assertThat(apuestaPerdedora.getEstado()).isEqualTo(EstadoApuesta.PERDIDA);
        assertThat(apostadorGanador.getSaldo()).isEqualTo(20000.0);
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.LIQUIDADO);

        verify(usuarioRepository, never()).findById(21L);
        verify(movimientoSaldoRepository, times(1)).save(any(MovimientoSaldo.class));
        verify(eventoRepository).save(evento);
    }

    @Test
    @DisplayName("liquidarEvento() retorna lista vacía y transiciona el evento a LIQUIDADO "
            + "cuando no tiene apuestas REGISTRADA")
    void liquidarEvento_sinApuestasRegistradas_retornaListaVaciaYLiquidaEvento() {
        Evento evento = eventoConEstado(60L, EstadoEvento.CERRADO);

        when(apuestaRepository.findByOpcionMercadoEventoIdAndEstado(60L, EstadoApuesta.REGISTRADA))
                .thenReturn(List.of());
        when(eventoRepository.findById(60L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        List<Apuesta> resultado = apuestaService.liquidarEvento(60L, 999L);

        assertThat(resultado).isEmpty();
        assertThat(evento.getEstado()).isEqualTo(EstadoEvento.LIQUIDADO);
        verify(apuestaRepository, never()).save(any(Apuesta.class));
        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
    }

    @Test
    @DisplayName("liquidarEvento() propaga la excepción cuando el evento no puede transicionar a LIQUIDADO")
    void liquidarEvento_eventoNoPuedeLiquidarse_propagaExcepcion() {
        Evento evento = eventoConEstado(70L, EstadoEvento.ABIERTO);

        when(apuestaRepository.findByOpcionMercadoEventoIdAndEstado(70L, EstadoApuesta.REGISTRADA))
                .thenReturn(List.of());
        when(eventoRepository.findById(70L)).thenReturn(Optional.of(evento));

        assertThatThrownBy(() -> apuestaService.liquidarEvento(70L, 999L))
                .isInstanceOf(TransicionEstadoInvalidaException.class);

        verify(eventoRepository, never()).save(any(Evento.class));
    }

    private static Apuesta apuestaConDetalles(Long id, EstadoApuesta estado, Usuario apostador, Long eventoId,
            LocalDateTime creadoEn) {
        Evento evento = new Evento();
        evento.setId(eventoId);

        Mercado mercado = new Mercado();
        mercado.setEvento(evento);

        OpcionApuesta opcion = new OpcionApuesta();
        opcion.setMercado(mercado);

        Apuesta apuesta = new Apuesta();
        apuesta.setId(id);
        apuesta.setEstado(estado);
        apuesta.setApostador(apostador);
        apuesta.setOpcion(opcion);
        apuesta.setCreadoEn(creadoEn);
        return apuesta;
    }

    @Test
    @DisplayName("buscarHistorial() sin filtros retorna todas las apuestas")
    void buscarHistorial_sinFiltros_retornaTodasLasApuestas() {
        Usuario apostador = apostadorConSaldo(1L, 10000.0);
        Apuesta apuesta1 = apuestaConDetalles(1L, EstadoApuesta.REGISTRADA, apostador, 10L,
                LocalDateTime.of(2026, 6, 1, 10, 0));
        Apuesta apuesta2 = apuestaConDetalles(2L, EstadoApuesta.PAGADA, apostador, 20L,
                LocalDateTime.of(2026, 6, 15, 10, 0));

        when(apuestaRepository.findAll()).thenReturn(List.of(apuesta1, apuesta2));

        List<Apuesta> resultado = apuestaService.buscarHistorial(null, null, null, null, null);

        assertThat(resultado).containsExactlyInAnyOrder(apuesta1, apuesta2);
    }

    @Test
    @DisplayName("buscarHistorial() combina filtros de apostador, evento y estado y solo retorna la coincidencia")
    void buscarHistorial_combinaFiltrosApostadorEventoYEstado_retornaSoloCoincidencia() {
        Usuario apostadorUno = apostadorConSaldo(1L, 10000.0);
        Usuario apostadorDos = apostadorConSaldo(2L, 10000.0);

        Apuesta coincide = apuestaConDetalles(1L, EstadoApuesta.GANADA, apostadorUno, 10L,
                LocalDateTime.of(2026, 6, 10, 10, 0));
        Apuesta otroApostador = apuestaConDetalles(2L, EstadoApuesta.GANADA, apostadorDos, 10L,
                LocalDateTime.of(2026, 6, 10, 10, 0));
        Apuesta otroEvento = apuestaConDetalles(3L, EstadoApuesta.GANADA, apostadorUno, 20L,
                LocalDateTime.of(2026, 6, 10, 10, 0));
        Apuesta otroEstado = apuestaConDetalles(4L, EstadoApuesta.PERDIDA, apostadorUno, 10L,
                LocalDateTime.of(2026, 6, 10, 10, 0));

        when(apuestaRepository.findAll())
                .thenReturn(List.of(coincide, otroApostador, otroEvento, otroEstado));

        List<Apuesta> resultado = apuestaService.buscarHistorial(1L, 10L, EstadoApuesta.GANADA, null, null);

        assertThat(resultado).containsExactly(coincide);
    }

    @Test
    @DisplayName("buscarHistorial() filtra por rango de fechas de forma inclusiva en ambos límites")
    void buscarHistorial_filtraPorRangoDeFechas_esInclusivoEnAmbosLimites() {
        Usuario apostador = apostadorConSaldo(1L, 10000.0);
        Apuesta antesDelRango = apuestaConDetalles(1L, EstadoApuesta.REGISTRADA, apostador, 10L,
                LocalDateTime.of(2026, 5, 31, 23, 59));
        Apuesta enElLimiteInferior = apuestaConDetalles(2L, EstadoApuesta.REGISTRADA, apostador, 10L,
                LocalDateTime.of(2026, 6, 1, 0, 0));
        Apuesta enElLimiteSuperior = apuestaConDetalles(3L, EstadoApuesta.REGISTRADA, apostador, 10L,
                LocalDateTime.of(2026, 6, 10, 23, 59));
        Apuesta despuesDelRango = apuestaConDetalles(4L, EstadoApuesta.REGISTRADA, apostador, 10L,
                LocalDateTime.of(2026, 6, 11, 0, 1));

        when(apuestaRepository.findAll())
                .thenReturn(List.of(antesDelRango, enElLimiteInferior, enElLimiteSuperior, despuesDelRango));

        List<Apuesta> resultado = apuestaService.buscarHistorial(
                null, null, null, LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 10));

        assertThat(resultado).containsExactlyInAnyOrder(enElLimiteInferior, enElLimiteSuperior);
    }

    @Test
    @DisplayName("obtenerHistorialApostador() retorna el saldo actual, las apuestas y los movimientos del apostador")
    void obtenerHistorialApostador_usuarioExistente_retornaSaldoApuestasYMovimientos() {
        Usuario apostador = apostadorConSaldo(30L, 15000.0);
        Apuesta apuesta = apuestaConEstado(1L, EstadoApuesta.REGISTRADA, apostador, 5000.0, 2.0);
        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(apostador);
        movimiento.setTipo("RECARGA");
        movimiento.setMonto(20000.0);

        when(usuarioRepository.findById(30L)).thenReturn(Optional.of(apostador));
        when(apuestaRepository.findByApostadorId(30L)).thenReturn(List.of(apuesta));
        when(movimientoSaldoRepository.findByUsuarioId(30L)).thenReturn(List.of(movimiento));

        HistorialApostadorResponse resultado = apuestaService.obtenerHistorialApostador(30L);

        assertThat(resultado.saldo()).isEqualTo(15000.0);
        assertThat(resultado.apuestas()).containsExactly(apuesta);
        assertThat(resultado.movimientos()).containsExactly(movimiento);
    }

    @Test
    @DisplayName("obtenerHistorialApostador() lanza IllegalArgumentException cuando el usuario no existe")
    void obtenerHistorialApostador_usuarioInexistente_lanzaExcepcion() {
        when(usuarioRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> apuestaService.obtenerHistorialApostador(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuario apostador no encontrado");

        verify(apuestaRepository, never()).findByApostadorId(any());
    }
}
