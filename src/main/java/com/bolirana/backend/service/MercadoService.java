package com.bolirana.backend.service;

import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.domain.HistorialCuota;
import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.domain.OpcionApuesta;
import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.exception.RecursoNoEncontradoException;
import com.bolirana.backend.exception.ValidacionNegocioException;
import com.bolirana.backend.repository.HistorialCuotaRepository;
import com.bolirana.backend.repository.MercadoRepository;
import com.bolirana.backend.repository.OpcionApuestaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Servicio unificado encargado de la gestión de mercados y el control de cuotas de apuestas.
 * RF-23: El Administrador debe poder cambiar las cuotas de un mercado mientras el evento esté ABIERTO.
 */
@Service
@RequiredArgsConstructor
public class MercadoService {

    private final MercadoRepository mercadoRepository;
    private final OpcionApuestaRepository opcionApuestaRepository;
    private final HistorialCuotaRepository historialRepository;

    // --- OPERACIONES DE MERCADO (De MercadoService1) ---

    /** Retorna la lista de todos los mercados registrados en el sistema. */
    @Transactional(readOnly = true)
    public List<Mercado> listar() {
        return mercadoRepository.findAll();
    }

    /** * Busca y retorna un mercado por su identificador único. 
     * Lanza excepción explícita si no existe en lugar de retornar un Optional vacío.
     */
    @Transactional(readOnly = true)
    public Mercado buscarPorId(Long id) {
        return mercadoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el mercado con id " + id));
    }

    /** Retorna los mercados asociados a un evento específico. */
    @Transactional(readOnly = true)
    public List<Mercado> listarPorEvento(Long eventoId) {
        return mercadoRepository.findByEventoId(eventoId);
    }

    /** Crea y persiste un nuevo mercado. */
    @Transactional
    public Mercado crear(Mercado mercado) {
        return mercadoRepository.save(mercado);
    }


    // --- OPERACIONES DE CUOTAS Y LÓGICA DE NEGOCIO (De MercadoService) ---

    /**
     * RF-23: Cambia la cuota final de una opción de apuesta, validando
     * que el evento al que pertenece esté en estado ABIERTO, y registra
     * el cambio (cuota anterior y nueva) en el historial.
     *
     * @throws ValidacionNegocioException si nuevaCuota es nula o no es mayor a 1.0 (RNF-08)
     */
    @Transactional
    public OpcionApuesta cambiarCuota(Long opcionApuestaId, BigDecimal nuevaCuota) {
        if (nuevaCuota == null || nuevaCuota.compareTo(BigDecimal.ONE) <= 0) {
            throw new ValidacionNegocioException("La cuota debe ser mayor a 1.0");
        }

        OpcionApuesta opcion = opcionApuestaRepository.findById(opcionApuestaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No se encontró la opción de apuesta con id " + opcionApuestaId));

        Evento evento = opcion.getMercado().getEvento();

        if (evento.getEstado() != EstadoEvento.ABIERTO) {
            throw new ValidacionNegocioException(
                    "No se puede cambiar la cuota: el evento debe estar en estado ABIERTO (estado actual: "
                            + evento.getEstado() + ")");
        }

        BigDecimal cuotaAnterior = opcion.getCuotaActual();

        // RF-23: registrar el cambio con cuota anterior y nueva, origen MANUAL
        HistorialCuota historial = new HistorialCuota(
                opcion, cuotaAnterior, nuevaCuota, HistorialCuota.ORIGEN_MANUAL);
        historialRepository.save(historial);

        opcion.setCuotaActual(nuevaCuota);

        return opcionApuestaRepository.save(opcion);
    }

    /**
     * RF-23: Consulta el historial de cambios de cuota de una opción de apuesta.
     */
    @Transactional(readOnly = true)
    public List<HistorialCuota> obtenerHistorialCuotas(Long opcionApuestaId) {
        if (!opcionApuestaRepository.existsById(opcionApuestaId)) {
            throw new RecursoNoEncontradoException(
                    "No se encontró la opción de apuesta con id " + opcionApuestaId);
        }
        return historialRepository.findByOpcionApuestaIdOrderByCambiadoEnDesc(opcionApuestaId);
    }
}
