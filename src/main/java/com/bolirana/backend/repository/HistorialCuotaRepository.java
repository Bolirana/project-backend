package com.bolirana.backend.repository;

import com.bolirana.backend.domain.HistorialCuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialCuotaRepository extends JpaRepository<HistorialCuota, Long> {

    /**
     * Busca el historial de cuotas registrado para una opción de apuesta.
     *
     * @param opcionId identificador de la opción de apuesta
     * @return lista del historial de cuotas asociado a la opción
     */
    List<HistorialCuota> findByOpcionId(Long opcionId);
}
