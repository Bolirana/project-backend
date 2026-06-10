package com.bolirana.backend.repository;

import com.bolirana.backend.domain.Mercado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MercadoRepository extends JpaRepository<Mercado, Long> {

    List<Mercado> findByEventoId(Long eventoId);
}
