package com.bolirana.backend.service;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.EstadoApuesta;
import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.repository.ApuestaRepository;
import com.bolirana.backend.repository.OpcionApuestaRepository;
import com.bolirana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ApuestaService {

    private final ApuestaRepository apuestaRepository;
    private final OpcionApuestaRepository opcionApuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoSaldoService movimientoSaldoService;

    /** Retorna la lista de todas las apuestas registradas en el sistema. */
    public List<Apuesta> listar() {
        return apuestaRepository.findAll();
    }

    /**
     * Busca y retorna una apuesta por su identificador único.
     *
     * @param id identificador de la apuesta
     * @return Optional con la apuesta si existe, vacío si no se encuentra
     */
    public Optional<Apuesta> buscarPorId(Long id) {
        return apuestaRepository.findById(id);
    }

    /**
     * Retorna las apuestas realizadas por un apostador específico.
     *
     * @param apostadorId identificador del usuario apostador
     * @return lista de apuestas realizadas por el apostador
     */
    public List<Apuesta> listarPorApostador(Long apostadorId) {
        return apuestaRepository.findByApostadorId(apostadorId);
    }

    /**
     * Registra una nueva apuesta, congelando la cuota vigente de la opción
     * seleccionada, descontando el saldo del apostador y generando el
     * movimiento de saldo correspondiente.
     *
     * @param apuesta datos de la apuesta a registrar
     * @return la apuesta creada y persistida
     * @throws IllegalArgumentException si la opción de apuesta no existe, si el evento
     *         asociado no está en estado ABIERTO, si el apostador no existe o si su
     *         saldo es insuficiente para cubrir el monto de la apuesta
     */
    @Transactional
    public Apuesta crear(Apuesta apuesta) {
        OpcionApuesta opcion = opcionApuestaRepository.findById(apuesta.getOpcion().getId())
                .orElseThrow(() -> new IllegalArgumentException("Opcion de apuesta no encontrada"));

        String estadoEvento = opcion.getMercado().getEvento().getEstado();
        if (!"ABIERTO".equals(estadoEvento)) {
            throw new IllegalArgumentException("No se puede apostar: el evento no está ABIERTO");
        }

        Usuario apostador = usuarioRepository.findById(apuesta.getApostador().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario apostador no encontrado"));

        double saldoActual = apostador.getSaldo() != null ? apostador.getSaldo() : 0.0;
        if (saldoActual < apuesta.getMonto()) {
            throw new IllegalArgumentException("Saldo insuficiente para realizar la apuesta");
        }

        apuesta.setApostador(apostador);
        apuesta.setOpcion(opcion);
        apuesta.setCuotaCongelada(opcion.getCuotaActual());
        apuesta.setEstado(EstadoApuesta.REGISTRADA);

        Apuesta apuestaGuardada = apuestaRepository.save(apuesta);

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(apostador);
        movimiento.setTipo("APUESTA");
        movimiento.setMonto(apuesta.getMonto());
        movimientoSaldoService.crear(movimiento);

        return apuestaGuardada;
    }
}
