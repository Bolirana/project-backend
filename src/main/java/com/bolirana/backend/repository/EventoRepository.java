package com.bolirana.backend.repository;

import com.bolirana.backend.domain.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    /**
     * Busca los eventos que se encuentran en un estado específico.
     *
     * @param estado estado del evento (por ejemplo: ABIERTO, CERRADO, FINALIZADO)
     * @return lista de eventos que coinciden con el estado indicado
     */
    List<Evento> findByEstado(String estado);
}
