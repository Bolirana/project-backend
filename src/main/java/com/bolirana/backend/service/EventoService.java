package com.bolirana.backend.service;

import com.bolirana.backend.builder.EventoBuilder;
import com.bolirana.backend.dto.EventoCreacionDTO;
import com.bolirana.backend.dto.MercadoDTO;
import com.bolirana.backend.dto.OpcionApuestaDTO;
import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.exception.RecursoNoEncontradoException;
import com.bolirana.backend.exception.TransicionEstadoInvalidaException;
import com.bolirana.backend.exception.ValidacionNegocioException;
import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio unificado encargado de la gestion de eventos deportivos.
 * Incorpora reglas de negocio robustas, manejo de DTOs, validacion de estados y Lombok.
 */
@Service
@RequiredArgsConstructor // Combines the clean boilerplate reduction from File 2
public class EventoService {

    private final EventoRepository eventoRepository;

    /**
     * RF-04: Crea un evento deportivo con al menos un mercado y sus opciones de apuesta.
     * RNF-02: La construccion del evento se realiza mediante el patron Builder.
     */
    @Transactional
    public Evento crearEvento(EventoCreacionDTO dto) {
        if (dto.getFechaEvento() != null && dto.getFechaEvento().isBefore(LocalDate.now())) {
            throw new ValidacionNegocioException("La fecha del evento no puede ser anterior a la fecha actual");
        }

        if (dto.getMercados() == null || dto.getMercados().isEmpty()) {
            throw new ValidacionNegocioException("El evento debe tener al menos un mercado con sus opciones de apuesta");
        }

        EventoBuilder eventoBuilder = new EventoBuilder()
                .conEquipoLocal(dto.getEquipoLocal())
                .conEquipoVisitante(dto.getEquipoVisitante())
                .conDeporte(dto.getDeporte())
                .conFechaEvento(dto.getFechaEvento())
                .conIdEquipoLocalExterno(dto.getIdEquipoLocalExterno())
                .conIdEquipoVisitanteExterno(dto.getIdEquipoVisitanteExterno())
                .conIdEventoExternoOdds(dto.getIdEventoExternoOdds());

        for (MercadoDTO mercadoDTO : dto.getMercados()) {
            if (mercadoDTO.getOpciones() == null || mercadoDTO.getOpciones().isEmpty()) {
                throw new ValidacionNegocioException(
                        "El mercado '" + mercadoDTO.getNombre() + "' debe tener al menos una opción de apuesta");
            }

            EventoBuilder.MercadoBuilder mercadoBuilder = new EventoBuilder.MercadoBuilder(mercadoDTO.getNombre());

            for (OpcionApuestaDTO opcionDTO : mercadoDTO.getOpciones()) {
                mercadoBuilder.agregarOpcion(
                        opcionDTO.getNombre(),
                        opcionDTO.getCuotaReferencia(),
                        opcionDTO.getCuotaActual()
                );
            }

            Mercado mercado = mercadoBuilder.build();
            eventoBuilder.agregarMercado(mercado);
        }

        Evento evento = eventoBuilder.build();
        return eventoRepository.save(evento);
    }

    /** Retorna la lista de todos los eventos registrados en el sistema. */
    @Transactional(readOnly = true)
    public List<Evento> listarEventos() {
        return eventoRepository.findAll();
    }

    /**
     * Busca y retorna un evento por su identificador unico.
     * Lanza una excepcion controlada si no se encuentra.
     */
    @Transactional(readOnly = true)
    public Evento obtenerEventoPorId(Long id) {
        return eventoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No se encontró el evento con id " + id));
    }

    /**
     * Retorna los eventos que se encuentran en un estado específico.
     * (Adoptado de File 2, pero mejorado usando Type-Safe Enum)
     */
    @Transactional(readOnly = true)
    public List<Evento> listarPorEstado(EstadoEvento estado) {
        return eventoRepository.findByEstado(estado);
    }

    /**
     * RF-05 / RF-16: Cambia el estado de un evento validando que la transición
     * sea permitida según el flujo de negocio establecido.
     */
    @Transactional
    public Evento cambiarEstado(Long id, EstadoEvento nuevoEstado) {
        Evento evento = obtenerEventoPorId(id);
        EstadoEvento estadoActual = evento.getEstado();

        if (estadoActual == nuevoEstado) {
            throw new TransicionEstadoInvalidaException(
                    "El evento ya se encuentra en el estado " + nuevoEstado);
        }

        if (!estadoActual.puedeTransicionarA(nuevoEstado)) {
            throw new TransicionEstadoInvalidaException(
                    "Transacción inválida: no se puede cambiar el evento de estado "
                            + estadoActual + " a " + nuevoEstado);
        }

        evento.setEstado(nuevoEstado);
        return eventoRepository.save(evento);
    }
}