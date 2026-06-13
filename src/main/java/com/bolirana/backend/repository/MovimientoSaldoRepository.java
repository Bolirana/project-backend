package com.bolirana.backend.repository;

import com.bolirana.backend.domain.MovimientoSaldo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoSaldoRepository extends JpaRepository<MovimientoSaldo, Long> {

    /**
     * Busca los movimientos de saldo registrados para un usuario.
     *
     * @param usuarioId identificador del usuario
     * @return lista de movimientos de saldo del usuario
     */
    List<MovimientoSaldo> findByUsuarioId(Long usuarioId);
}
