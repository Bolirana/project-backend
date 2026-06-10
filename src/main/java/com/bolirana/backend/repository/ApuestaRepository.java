package com.bolirana.backend.repository;

import com.bolirana.backend.domain.Apuesta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ApuestaRepository extends JpaRepository<Apuesta, Long> {

    List<Apuesta> findByApostadorId(Long apostadorId);
}
