package com.bolirana.backend.repository;

import com.bolirana.backend.domain.Evento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventoRepository extends JpaRepository<Evento, Long> {

    List<Evento> findByEstado(String estado);
}
