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

    public List<MovimientoSaldo> listar() {
        return movimientoSaldoRepository.findAll();
    }

    public List<MovimientoSaldo> listarPorUsuario(Long usuarioId) {
        return movimientoSaldoRepository.findByUsuarioId(usuarioId);
    }

    public MovimientoSaldo crear(MovimientoSaldo movimientoSaldo) {
        return movimientoSaldoRepository.save(movimientoSaldo);
    }
}
