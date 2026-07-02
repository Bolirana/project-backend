package com.bolirana.backend.service;

import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.repository.MovimientoSaldoRepository;
import com.bolirana.backend.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
}
