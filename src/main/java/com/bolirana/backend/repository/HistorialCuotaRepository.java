package com.bolirana.backend.repository;

import com.bolirana.backend.domain.HistorialCuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialCuotaRepository extends JpaRepository<HistorialCuota, Long> {

    /**
     * Busca el historial de cambios de cuota de una opción de apuesta,
     * ordenado del más reciente al más antiguo.
     *
     * @param opcionApuestaId identificador de la opción de apuesta
     * @return historial de cuotas de la opción, ordenado por fecha descendente
     */
    List<HistorialCuota> findByOpcionApuestaIdOrderByCambiadoEnDesc(Long opcionApuestaId);
}
