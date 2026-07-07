package com.bolirana.backend.service;

import com.bolirana.backend.domain.EstadoUsuario;
import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.repository.MovimientoSaldoRepository;
import com.bolirana.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoSaldoServiceTest {

    @Mock
    private MovimientoSaldoRepository movimientoSaldoRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MovimientoSaldoService movimientoSaldoService;

    @Test
    @DisplayName("listarPorUsuario retorna la lista de movimientos filtrada por usuarioId")
    void listarPorUsuario_usuarioConMovimientos_retornaListaFiltrada() {
        // Caso límite: el repositorio debe devolver únicamente los movimientos asociados al usuarioId solicitado
        Long usuarioId = 5L;

        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);

        MovimientoSaldo deposito = new MovimientoSaldo();
        deposito.setId(1L);
        deposito.setUsuario(usuario);
        deposito.setTipo("DEPOSITO");
        deposito.setMonto(100000.0);

        MovimientoSaldo retiro = new MovimientoSaldo();
        retiro.setId(2L);
        retiro.setUsuario(usuario);
        retiro.setTipo("RETIRO");
        retiro.setMonto(50000.0);

        when(movimientoSaldoRepository.findByUsuarioId(usuarioId))
                .thenReturn(List.of(deposito, retiro));

        List<MovimientoSaldo> resultado = movimientoSaldoService.listarPorUsuario(usuarioId);

        assertThat(resultado)
                .hasSize(2)
                .containsExactly(deposito, retiro)
                .allSatisfy(movimiento -> assertThat(movimiento.getUsuario().getId()).isEqualTo(usuarioId));
        verify(movimientoSaldoRepository).findByUsuarioId(usuarioId);
    }

    @Test
    @DisplayName("crear() con tipo RECARGA suma el monto al saldo del usuario")
    void crear_tipoRecarga_sumaMontoAlSaldo() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(10000.0);

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(usuario);
        movimiento.setTipo("RECARGA");
        movimiento.setMonto(5000.0);
        movimiento.setMetodoPago("NEQUI");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(movimientoSaldoRepository.save(movimiento)).thenReturn(movimiento);

        movimientoSaldoService.crear(movimiento);

        assertThat(usuario.getSaldo()).isEqualTo(15000.0);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("crear() con tipo APUESTA resta el monto al saldo del usuario")
    void crear_tipoApuesta_restaMontoAlSaldo() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(10000.0);

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(usuario);
        movimiento.setTipo("APUESTA");
        movimiento.setMonto(4000.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(movimientoSaldoRepository.save(movimiento)).thenReturn(movimiento);

        movimientoSaldoService.crear(movimiento);

        assertThat(usuario.getSaldo()).isEqualTo(6000.0);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("crear() con tipo RETIRO resta el monto al saldo del usuario")
    void crear_tipoRetiro_restaMontoAlSaldo() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(10000.0);

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(usuario);
        movimiento.setTipo("RETIRO");
        movimiento.setMonto(3000.0);
        movimiento.setMetodoPago("NEQUI");

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(movimientoSaldoRepository.save(movimiento)).thenReturn(movimiento);

        movimientoSaldoService.crear(movimiento);

        assertThat(usuario.getSaldo()).isEqualTo(7000.0);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("crear() con tipo PAGO_APUESTA suma el monto al saldo del usuario")
    void crear_tipoPagoApuesta_sumaMontoAlSaldo() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(10000.0);

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(usuario);
        movimiento.setTipo("PAGO_APUESTA");
        movimiento.setMonto(6000.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(movimientoSaldoRepository.save(movimiento)).thenReturn(movimiento);

        movimientoSaldoService.crear(movimiento);

        assertThat(usuario.getSaldo()).isEqualTo(16000.0);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    @DisplayName("crear() con usuario sin saldo previo (null) lo trata como 0.0")
    void crear_saldoPrevioNulo_loTrataComoCero() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(null);

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(usuario);
        movimiento.setTipo("RECARGA");
        movimiento.setMonto(2000.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(movimientoSaldoRepository.save(movimiento)).thenReturn(movimiento);

        movimientoSaldoService.crear(movimiento);

        assertThat(usuario.getSaldo()).isEqualTo(2000.0);
    }

    @Test
    @DisplayName("crear() lanza IllegalArgumentException y no guarda cuando el usuario no existe")
    void crear_usuarioInexistente_lanzaExcepcionSinGuardar() {
        Usuario usuarioInexistente = new Usuario();
        usuarioInexistente.setId(99L);

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(usuarioInexistente);
        movimiento.setTipo("RECARGA");
        movimiento.setMonto(1000.0);

        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movimientoSaldoService.crear(movimiento))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuario no encontrado");

        verify(movimientoSaldoRepository, never()).save(movimiento);
    }

    @Test
    @DisplayName("crear() lanza IllegalArgumentException y no guarda cuando el tipo no es reconocido")
    void crear_tipoNoReconocido_lanzaExcepcionSinGuardar() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(10000.0);

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(usuario);
        movimiento.setTipo("TRANSFERENCIA");
        movimiento.setMonto(1000.0);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> movimientoSaldoService.crear(movimiento))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de movimiento no reconocido: TRANSFERENCIA");

        verify(movimientoSaldoRepository, never()).save(movimiento);
        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    @DisplayName("recargar() con metodoPago válido crea el movimiento RECARGA y suma el saldo")
    void recargar_metodoPagoValido_creaMovimientoYSumaSaldo() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(10000.0);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(movimientoSaldoRepository.save(any(MovimientoSaldo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        movimientoSaldoService.recargar(1L, 5000.0, "NEQUI");

        assertThat(usuario.getSaldo()).isEqualTo(15000.0);

        ArgumentCaptor<MovimientoSaldo> captor = ArgumentCaptor.forClass(MovimientoSaldo.class);
        verify(movimientoSaldoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("RECARGA");
        assertThat(captor.getValue().getMonto()).isEqualTo(5000.0);
        assertThat(captor.getValue().getMetodoPago()).isEqualTo("NEQUI");
    }

    @Test
    @DisplayName("recargar() con metodoPago inválido lanza IllegalArgumentException sin tocar los repositorios")
    void recargar_metodoPagoInvalido_lanzaExcepcionSinTocarRepositorios() {
        assertThatThrownBy(() -> movimientoSaldoService.recargar(1L, 5000.0, "BITCOIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Método de pago no válido: BITCOIN");

        verifyNoInteractions(usuarioRepository, movimientoSaldoRepository);
    }

    @Test
    @DisplayName("recargar() con usuario no ACTIVO lanza IllegalArgumentException sin crear el movimiento")
    void recargar_usuarioNoActivo_lanzaExcepcionSinCrearMovimiento() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(10000.0);
        usuario.setEstado(EstadoUsuario.SUSPENDIDO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> movimientoSaldoService.recargar(1L, 5000.0, "NEQUI"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El usuario no puede realizar movimientos de saldo: cuenta suspendida o eliminada");

        assertThat(usuario.getSaldo()).isEqualTo(10000.0);
        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    @DisplayName("retirar() con saldo suficiente resta el saldo y crea el movimiento RETIRO")
    void retirar_saldoSuficiente_restaSaldoYCreaMovimiento() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(10000.0);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(movimientoSaldoRepository.save(any(MovimientoSaldo.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        movimientoSaldoService.retirar(1L, 4000.0, "NEQUI");

        assertThat(usuario.getSaldo()).isEqualTo(6000.0);

        ArgumentCaptor<MovimientoSaldo> captor = ArgumentCaptor.forClass(MovimientoSaldo.class);
        verify(movimientoSaldoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo("RETIRO");
        assertThat(captor.getValue().getMonto()).isEqualTo(4000.0);
        assertThat(captor.getValue().getMetodoPago()).isEqualTo("NEQUI");
    }

    @Test
    @DisplayName("retirar() con metodoPago inválido lanza IllegalArgumentException sin tocar los repositorios")
    void retirar_metodoPagoInvalido_lanzaExcepcionSinTocarRepositorios() {
        assertThatThrownBy(() -> movimientoSaldoService.retirar(1L, 5000.0, "BITCOIN"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Método de pago no válido: BITCOIN");

        verifyNoInteractions(usuarioRepository, movimientoSaldoRepository);
    }

    @Test
    @DisplayName("retirar() con saldo insuficiente lanza IllegalArgumentException sin guardar")
    void retirar_saldoInsuficiente_lanzaExcepcionSinGuardar() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(1000.0);
        usuario.setEstado(EstadoUsuario.ACTIVO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> movimientoSaldoService.retirar(1L, 5000.0, "NEQUI"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Saldo insuficiente para realizar el retiro");

        assertThat(usuario.getSaldo()).isEqualTo(1000.0);
        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
        verify(usuarioRepository, never()).save(usuario);
    }

    @Test
    @DisplayName("retirar() con usuario inexistente lanza IllegalArgumentException sin guardar")
    void retirar_usuarioInexistente_lanzaExcepcionSinGuardar() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> movimientoSaldoService.retirar(99L, 1000.0, "NEQUI"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Usuario no encontrado");

        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
    }

    @Test
    @DisplayName("retirar() con usuario no ACTIVO lanza IllegalArgumentException sin guardar")
    void retirar_usuarioNoActivo_lanzaExcepcionSinGuardar() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setSaldo(10000.0);
        usuario.setEstado(EstadoUsuario.ELIMINADO);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> movimientoSaldoService.retirar(1L, 5000.0, "NEQUI"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("El usuario no puede realizar movimientos de saldo: cuenta suspendida o eliminada");

        assertThat(usuario.getSaldo()).isEqualTo(10000.0);
        verify(movimientoSaldoRepository, never()).save(any(MovimientoSaldo.class));
        verify(usuarioRepository, never()).save(usuario);
    }
}
