package com.bolirana.backend.service;

import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.repository.MovimientoSaldoRepository;
import com.bolirana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovimientoSaldoService {

    private final MovimientoSaldoRepository movimientoSaldoRepository;
    private final UsuarioRepository usuarioRepository;

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
     * Registra un nuevo movimiento de saldo y actualiza el saldo del usuario:
     * lo suma si el tipo es RECARGA, lo resta si es APUESTA o RETIRO.
     *
     * @param movimientoSaldo datos del movimiento de saldo a registrar
     * @return el movimiento de saldo creado y persistido
     * @throws IllegalArgumentException si el usuario no existe o el tipo de movimiento no es reconocido
     */
    @Transactional
    public MovimientoSaldo crear(MovimientoSaldo movimientoSaldo) {
        Usuario usuario = usuarioRepository.findById(movimientoSaldo.getUsuario().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        double saldoActual = usuario.getSaldo() != null ? usuario.getSaldo() : 0.0;

        switch (movimientoSaldo.getTipo()) {
            case "RECARGA" -> usuario.setSaldo(saldoActual + movimientoSaldo.getMonto());
            case "APUESTA", "RETIRO" -> usuario.setSaldo(saldoActual - movimientoSaldo.getMonto());
            default -> throw new IllegalArgumentException(
                    "Tipo de movimiento no reconocido: " + movimientoSaldo.getTipo());
        }

        usuarioRepository.save(usuario);
        movimientoSaldo.setUsuario(usuario);
        return movimientoSaldoRepository.save(movimientoSaldo);
    }
}
