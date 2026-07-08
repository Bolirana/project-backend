package com.bolirana.backend.service;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.EstadoApuesta;
import com.bolirana.backend.domain.EstadoUsuario;
import com.bolirana.backend.domain.MovimientoSaldo;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.domain.Usuario;
import com.bolirana.backend.dto.HistorialApostadorResponse;
import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.repository.ApuestaRepository;
import com.bolirana.backend.repository.OpcionApuestaRepository;
import com.bolirana.backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApuestaService {

    private final ApuestaRepository apuestaRepository;
    private final OpcionApuestaRepository opcionApuestaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MovimientoSaldoService movimientoSaldoService;
    private final EventoService eventoService;

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
     * @throws IllegalArgumentException si el monto no es mayor a cero, si la opción de apuesta
     *         no existe, si el evento asociado no está en estado ABIERTO, si el apostador no
     *         existe, si su cuenta no está ACTIVA o si su saldo es insuficiente para cubrir
     *         el monto de la apuesta
     */
    @Transactional
    public Apuesta crear(Apuesta apuesta) {
        if (apuesta.getMonto() == null || apuesta.getMonto() <= 0) {
            throw new IllegalArgumentException("El monto de la apuesta debe ser mayor a cero");
        }

        OpcionApuesta opcion = opcionApuestaRepository.findById(apuesta.getOpcion().getId())
                .orElseThrow(() -> new IllegalArgumentException("Opcion de apuesta no encontrada"));

        EstadoEvento estadoEvento = opcion.getMercado().getEvento().getEstado();
        if (estadoEvento != EstadoEvento.ABIERTO) {
            throw new IllegalArgumentException("No se puede apostar: el evento no está ABIERTO");
        }

        Usuario apostador = usuarioRepository.findById(apuesta.getApostador().getId())
                .orElseThrow(() -> new IllegalArgumentException("Usuario apostador no encontrado"));

        if (apostador.getEstado() != EstadoUsuario.ACTIVO) {
            throw new IllegalArgumentException("El usuario no puede realizar apuestas: cuenta suspendida o eliminada");
        }

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
     * @throws IllegalArgumentException si la apuesta no existe, no está en estado GANADA,
     *         o el evento asociado aún no está en estado LIQUIDADO
     */
    @Transactional
    public Apuesta pagar(Long apuestaId) {
        Apuesta apuesta = apuestaRepository.findById(apuestaId)
                .orElseThrow(() -> new IllegalArgumentException("Apuesta no encontrada"));

        if (apuesta.getEstado() != EstadoApuesta.GANADA) {
            throw new IllegalArgumentException("Solo se puede pagar una apuesta en estado GANADA");
        }

        EstadoEvento estadoEvento = apuesta.getOpcion().getMercado().getEvento().getEstado();
        if (estadoEvento != EstadoEvento.LIQUIDADO) {
            throw new IllegalArgumentException("Solo se puede pagar una apuesta cuyo evento ya está LIQUIDADO");
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
     * RF-15: Liquida un evento transicionándolo primero a LIQUIDADO y luego
     * resolviendo todas sus apuestas REGISTRADA: las que apostaron a la opción
     * ganadora quedan GANADA y se pagan automáticamente; el resto queda PERDIDA.
     * El evento se liquida antes de resolver/pagar porque {@link #pagar(Long)}
     * exige que el evento asociado ya esté LIQUIDADO (RF-11).
     *
     * @param eventoId         identificador del evento a liquidar
     * @param opcionGanadoraId identificador de la opción de apuesta ganadora
     * @return las apuestas del evento tras la liquidación
     * @throws com.bolirana.backend.exception.TransicionEstadoInvalidaException si el evento
     *         no puede transicionar a LIQUIDADO desde su estado actual
     */
    @Transactional
    public List<Apuesta> liquidarEvento(Long eventoId, Long opcionGanadoraId) {
        List<Apuesta> apuestasRegistradas =
                apuestaRepository.findByOpcionMercadoEventoIdAndEstado(eventoId, EstadoApuesta.REGISTRADA);

        eventoService.cambiarEstado(eventoId, EstadoEvento.LIQUIDADO);

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

    /**
     * RF-21: Retorna el historial de apuestas filtrado opcionalmente por apostador,
     * evento, estado y rango de fechas de creación (ambos límites inclusivos).
     * Pensado para uso del Administrador.
     * NOTA: no valida el rol de quien llama porque el proyecto aún no tiene
     * autenticación por sesión/JWT (mismo pendiente documentado en UsuarioController).
     *
     * @param apostadorId filtro opcional por identificador del apostador
     * @param eventoId    filtro opcional por identificador del evento
     * @param estado      filtro opcional por estado de la apuesta
     * @param fechaDesde  filtro opcional: fecha mínima de creación (inclusive)
     * @param fechaHasta  filtro opcional: fecha máxima de creación (inclusive)
     * @return las apuestas que cumplen todos los filtros indicados
     */
    public List<Apuesta> buscarHistorial(Long apostadorId, Long eventoId, EstadoApuesta estado,
            LocalDate fechaDesde, LocalDate fechaHasta) {
        return apuestaRepository.findAll().stream()
                .filter(a -> apostadorId == null
                        || (a.getApostador() != null && apostadorId.equals(a.getApostador().getId())))
                .filter(a -> eventoId == null
                        || (a.getOpcion() != null && a.getOpcion().getMercado() != null
                                && a.getOpcion().getMercado().getEvento() != null
                                && eventoId.equals(a.getOpcion().getMercado().getEvento().getId())))
                .filter(a -> estado == null || estado == a.getEstado())
                .filter(a -> fechaDesde == null
                        || (a.getCreadoEn() != null && !a.getCreadoEn().toLocalDate().isBefore(fechaDesde)))
                .filter(a -> fechaHasta == null
                        || (a.getCreadoEn() != null && !a.getCreadoEn().toLocalDate().isAfter(fechaHasta)))
                .collect(Collectors.toList());
    }

    /**
     * RF-22: Retorna el historial completo de un apostador: sus apuestas, sus
     * movimientos de saldo y su saldo actual.
     *
     * @param apostadorId identificador del usuario apostador
     * @return el saldo actual, las apuestas y los movimientos de saldo del apostador
     * @throws IllegalArgumentException si el usuario apostador no existe
     */
    public HistorialApostadorResponse obtenerHistorialApostador(Long apostadorId) {
        Usuario apostador = usuarioRepository.findById(apostadorId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario apostador no encontrado"));

        List<Apuesta> apuestas = listarPorApostador(apostadorId);
        List<MovimientoSaldo> movimientos = movimientoSaldoService.listarPorUsuario(apostadorId);

        return new HistorialApostadorResponse(apostador.getSaldo(), apuestas, movimientos);
    }
}
