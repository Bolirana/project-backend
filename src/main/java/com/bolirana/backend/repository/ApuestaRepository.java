package com.bolirana.backend.repository;

import com.bolirana.backend.domain.Apuesta;
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
}
