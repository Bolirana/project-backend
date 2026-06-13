package com.bolirana.backend.service;

import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.repository.MovimientoSaldoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoSaldoService {

    private final MovimientoSaldoRepository movimientoSaldoRepository;

    /** Retorna la lista de todos los movimientos de saldo registrados en el sistema. */
    public List<MovimientoSaldo> listar() {
        return movimientoSaldoRepository.findAll();
    }

    /**
     * Retorna los movimientos de saldo registrados para un usuario.
     *
     * @param usuarioId identificador del usuario
     * @return lista de movimientos de saldo del usuario
     */
    public List<MovimientoSaldo> listarPorUsuario(Long usuarioId) {
        return movimientoSaldoRepository.findByUsuarioId(usuarioId);
    }

    /**
     * Registra un nuevo movimiento de saldo.
     *
     * @param movimientoSaldo datos del movimiento de saldo a registrar
     * @return el movimiento de saldo creado y persistido
     */
    public MovimientoSaldo crear(MovimientoSaldo movimientoSaldo) {
        return movimientoSaldoRepository.save(movimientoSaldo);
    }
}
