package com.bolirana.backend.service;

import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.repository.MovimientoSaldoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MovimientoSaldoServiceTest {

    @Mock
    private MovimientoSaldoRepository movimientoSaldoRepository;

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
}
