package com.bolirana.backend.repository;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.EstadoApuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApuestaRepository extends JpaRepository<Apuesta, Long> {

    /**
     * Busca las apuestas realizadas por un apostador.
     *
     * @param apostadorId identificador del usuario apostador
     * @return lista de apuestas realizadas por el apostador
     */
    List<Apuesta> findByApostadorId(Long apostadorId);

    /**
     * Busca las apuestas de un evento que se encuentran en un estado específico,
     * navegando la relación apuesta → opción → mercado → evento.
     *
     * @param eventoId identificador del evento
     * @param estado   estado de las apuestas a buscar
     * @return lista de apuestas del evento en el estado indicado
     */
    List<Apuesta> findByOpcionMercadoEventoIdAndEstado(Long eventoId, EstadoApuesta estado);
}
