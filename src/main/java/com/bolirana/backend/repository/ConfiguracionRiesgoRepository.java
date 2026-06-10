package com.bolirana.backend.repository;

import com.bolirana.backend.domain.ConfiguracionRiesgo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionRiesgoRepository extends JpaRepository<ConfiguracionRiesgo, Long> {

    Optional<ConfiguracionRiesgo> findByMercadoId(Long mercadoId);
}
