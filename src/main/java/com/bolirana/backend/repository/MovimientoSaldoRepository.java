package com.bolirana.backend.repository;

import com.bolirana.backend.domain.MovimientoSaldo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoSaldoRepository extends JpaRepository<MovimientoSaldo, Long> {

    List<MovimientoSaldo> findByUsuarioId(Long usuarioId);
}
