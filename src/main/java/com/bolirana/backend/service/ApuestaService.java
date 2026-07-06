package com.bolirana.backend.service;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.EstadoApuesta;
import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.repository.ApuestaRepository;
import com.bolirana.backend.repository.OpcionApuestaRepository;
import com.bolirana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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

        EstadoEvento estadoEvento = opcion.getMercado().getEvento().getEstado();
        if (estadoEvento != EstadoEvento.ABIERTO) {
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
        apuesta.setCuotaCongelada(opcion.getCuotaActual().doubleValue());
        apuesta.setEstado(EstadoApuesta.REGISTRADA);

        Apuesta apuestaGuardada = apuestaRepository.save(apuesta);

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(apostador);
        movimiento.setTipo("APUESTA");
        movimiento.setMonto(apuesta.getMonto());
        movimientoSaldoService.crear(movimiento);

        return apuestaGuardada;
    }

    /**
     * Resuelve una apuesta registrada, transicionándola a GANADA o PERDIDA.
     *
     * @param apuestaId identificador de la apuesta a resolver
     * @param resultado resultado de la apuesta, debe ser GANADA o PERDIDA
     * @return la apuesta actualizada
     * @throws IllegalArgumentException si la apuesta no existe, si el resultado no es
     *         GANADA ni PERDIDA, o si la apuesta no está en estado REGISTRADA
     */
    @Transactional
    public Apuesta resolver(Long apuestaId, EstadoApuesta resultado) {
        if (resultado != EstadoApuesta.GANADA && resultado != EstadoApuesta.PERDIDA) {
            throw new IllegalArgumentException("El resultado debe ser GANADA o PERDIDA");
        }

        Apuesta apuesta = apuestaRepository.findById(apuestaId)
                .orElseThrow(() -> new IllegalArgumentException("Apuesta no encontrada"));

        if (apuesta.getEstado() != EstadoApuesta.REGISTRADA) {
            throw new IllegalArgumentException("Solo se puede resolver una apuesta en estado REGISTRADA");
        }

        apuesta.setEstado(resultado);
        return apuestaRepository.save(apuesta);
    }

    /**
     * Paga una apuesta ganada: acredita al apostador el monto por la cuota
     * congelada y transiciona la apuesta a PAGADA.
     *
     * @param apuestaId identificador de la apuesta a pagar
     * @return la apuesta actualizada
     * @throws IllegalArgumentException si la apuesta no existe o no está en estado GANADA
     */
    @Transactional
    public Apuesta pagar(Long apuestaId) {
        Apuesta apuesta = apuestaRepository.findById(apuestaId)
                .orElseThrow(() -> new IllegalArgumentException("Apuesta no encontrada"));

        if (apuesta.getEstado() != EstadoApuesta.GANADA) {
            throw new IllegalArgumentException("Solo se puede pagar una apuesta en estado GANADA");
        }

        double montoAPagar = apuesta.getMonto() * apuesta.getCuotaCongelada();

        MovimientoSaldo movimiento = new MovimientoSaldo();
        movimiento.setUsuario(apuesta.getApostador());
        movimiento.setTipo("PAGO_APUESTA");
        movimiento.setMonto(montoAPagar);
        movimientoSaldoService.crear(movimiento);

        apuesta.setEstado(EstadoApuesta.PAGADA);
        return apuestaRepository.save(apuesta);
    }

    /**
     * RF-15: Liquida un evento resolviendo todas sus apuestas REGISTRADA:
     * las que apostaron a la opción ganadora quedan GANADA y se pagan
     * automáticamente; el resto queda PERDIDA.
     *
     * @param eventoId         identificador del evento a liquidar
     * @param opcionGanadoraId identificador de la opción de apuesta ganadora
     * @return las apuestas del evento tras la liquidación
     */
    @Transactional
    public List<Apuesta> liquidarEvento(Long eventoId, Long opcionGanadoraId) {
        List<Apuesta> apuestasRegistradas =
                apuestaRepository.findByOpcionMercadoEventoIdAndEstado(eventoId, EstadoApuesta.REGISTRADA);

        List<Apuesta> apuestasLiquidadas = new ArrayList<>();
        for (Apuesta apuesta : apuestasRegistradas) {
            boolean gano = apuesta.getOpcion().getId().equals(opcionGanadoraId);
            Apuesta resuelta = resolver(apuesta.getId(), gano ? EstadoApuesta.GANADA : EstadoApuesta.PERDIDA);

            if (resuelta.getEstado() == EstadoApuesta.GANADA) {
                resuelta = pagar(resuelta.getId());
            }

            apuestasLiquidadas.add(resuelta);
        }

        return apuestasLiquidadas;
    }
}
