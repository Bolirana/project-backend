package com.bolirana.backend.service;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.EstadoApuesta;
import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.repository.ApuestaRepository;
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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
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

    // Instancia real: MovimientoSaldoService no es una interfaz y en este entorno (JDK 26)
    // Mockito no puede instrumentar clases concretas, así que se colabora con la clase real
    // apoyada en sus propios repositorios mockeados en lugar de mockear el servicio.
    private ApuestaService apuestaService;

    @BeforeEach
    void setUp() {
        MovimientoSaldoService movimientoSaldoService =
                new MovimientoSaldoService(movimientoSaldoRepository, usuarioRepository);
        apuestaService = new ApuestaService(
                apuestaRepository, opcionApuestaRepository, usuarioRepository, movimientoSaldoService);
    }

    private static OpcionApuesta opcionConEvento(Long id, Double cuotaActual, String estadoEvento) {
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

    private static Usuario apostadorConSaldo(Long id, Double saldo) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setSaldo(saldo);
        return usuario;
    }

    @Test
    @DisplayName("crear() asigna cuotaCongelada igual a cuotaActual de la opción en el momento de registrar")
    void crear_opcionExistente_congelajCuotaActual() {
        // Caso límite: la cuota puede cambiar después; lo que se guarda debe ser el valor vigente al momento de crear
        OpcionApuesta opcion = opcionConEvento(1L, 2.5, "ABIERTO");
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
        OpcionApuesta opcion = opcionConEvento(2L, 1.8, "ABIERTO");
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
        OpcionApuesta opcion = opcionConEvento(3L, 2.0, "CERRADO");

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
        OpcionApuesta opcion = opcionConEvento(4L, 2.0, "ABIERTO");
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
    @DisplayName("crear() lanza IllegalArgumentException y no llama a save() cuando el saldo es insuficiente")
    void crear_saldoInsuficiente_lanzaExcepcionSinGuardar() {
        OpcionApuesta opcion = opcionConEvento(5L, 2.0, "ABIERTO");
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
        OpcionApuesta opcion = opcionConEvento(6L, 1.5, "ABIERTO");
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
}
