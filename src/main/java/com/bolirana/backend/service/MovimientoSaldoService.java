package com.bolirana.backend.service;

import com.bolirana.backend.domain.EstadoUsuario;
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

    private static final List<String> METODOS_PAGO_VALIDOS = List.of("NEQUI", "PSE", "TARJETA");

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
     * lo suma si el tipo es RECARGA o PAGO_APUESTA, lo resta si es APUESTA o RETIRO.
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
            case "RECARGA", "PAGO_APUESTA" -> usuario.setSaldo(saldoActual + movimientoSaldo.getMonto());
            case "APUESTA", "RETIRO" -> usuario.setSaldo(saldoActual - movimientoSaldo.getMonto());
            default -> throw new IllegalArgumentException(
                    "Tipo de movimiento no reconocido: " + movimientoSaldo.getTipo());
        }

        usuarioRepository.save(usuario);
        movimientoSaldo.setUsuario(usuario);
        return movimientoSaldoRepository.save(movimientoSaldo);
    }

    /**
     * Recarga saldo a un usuario mediante uno de los métodos de pago soportados.
     *
     * @param usuarioId  identificador del usuario a recargar
     * @param monto      monto a recargar
     * @param metodoPago método de pago utilizado (NEQUI, PSE o TARJETA)
     * @return el movimiento de saldo creado y persistido
     * @throws IllegalArgumentException si el usuario no existe, su cuenta no está ACTIVA
     *         o el método de pago no es válido
     */
    @Transactional
    public MovimientoSaldo recargar(Long usuarioId, Double monto, String metodoPago) {
        if (!METODOS_PAGO_VALIDOS.contains(metodoPago)) {
            throw new IllegalArgumentException("Método de pago no válido: " + metodoPago);
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new IllegalArgumentException(
                    "El usuario no puede realizar movimientos de saldo: cuenta suspendida o eliminada");
        }

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(usuario);
        movimiento.setTipo("RECARGA");
        movimiento.setMonto(monto);
        movimiento.setMetodoPago(metodoPago);

        return crear(movimiento);
    }

    /**
     * Retira saldo de un usuario, validando que tenga saldo suficiente.
     *
     * @param usuarioId  identificador del usuario a retirar
     * @param monto      monto a retirar
     * @param metodoPago método de retiro utilizado (NEQUI, PSE o TARJETA)
     * @return el movimiento de saldo creado y persistido
     * @throws IllegalArgumentException si el método de pago no es válido, el usuario no existe,
     *         su cuenta no está ACTIVA o no tiene saldo suficiente
     */
    @Transactional
    public MovimientoSaldo retirar(Long usuarioId, Double monto, String metodoPago) {
        if (!METODOS_PAGO_VALIDOS.contains(metodoPago)) {
            throw new IllegalArgumentException("Método de pago no válido: " + metodoPago);
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

        if (usuario.getEstado() != EstadoUsuario.ACTIVO) {
            throw new IllegalArgumentException(
                    "El usuario no puede realizar movimientos de saldo: cuenta suspendida o eliminada");
        }

        double saldoActual = usuario.getSaldo() != null ? usuario.getSaldo() : 0.0;
        if (saldoActual < monto) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar el retiro");
        }

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(usuario);
        movimiento.setTipo("RETIRO");
        movimiento.setMonto(monto);
        movimiento.setMetodoPago(metodoPago);

        return crear(movimiento);
    }
}
