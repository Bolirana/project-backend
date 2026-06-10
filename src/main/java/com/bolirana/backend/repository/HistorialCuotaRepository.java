package com.bolirana.backend.repository;

import com.bolirana.backend.domain.HistorialCuota;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistorialCuotaRepository extends JpaRepository<HistorialCuota, Long> {

    List<HistorialCuota> findByOpcionId(Long opcionId);
}
