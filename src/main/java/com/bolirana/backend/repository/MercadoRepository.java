package com.bolirana.backend.repository;

import com.bolirana.backend.domain.Mercado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MercadoRepository extends JpaRepository<Mercado, Long> {

    /**
     * Busca los mercados asociados a un evento.
     *
     * @param eventoId identificador del evento
     * @return lista de mercados asociados al evento
     */
    List<Mercado> findByEventoId(Long eventoId);
}
