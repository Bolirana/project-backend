package com.bolirana.backend.service;

import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.repository.EventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EventoService {

    private final EventoRepository eventoRepository;

    /** Retorna la lista de todos los eventos registrados en el sistema. */
    public List<Evento> listar() {
        return eventoRepository.findAll();
    }

    /**
     * Busca y retorna un evento por su identificador único.
     *
     * @param id identificador del evento
     * @return Optional con el evento si existe, vacío si no se encuentra
     */
    public Optional<Evento> buscarPorId(Long id) {
        return eventoRepository.findById(id);
    }

    /**
     * Retorna los eventos que se encuentran en un estado específico.
     *
     * @param estado estado del evento (por ejemplo: ABIERTO, CERRADO, FINALIZADO)
     * @return lista de eventos que coinciden con el estado indicado
     */
    public List<Evento> listarPorEstado(String estado) {
        return eventoRepository.findByEstado(estado);
    }

    /**
     * Crea y persiste un nuevo evento.
     *
     * @param evento datos del evento a crear
     * @return el evento creado y persistido
     */
    public Evento crear(Evento evento) {
        return eventoRepository.save(evento);
    }
}
