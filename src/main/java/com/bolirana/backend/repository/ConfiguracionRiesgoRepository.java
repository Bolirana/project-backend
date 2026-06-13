package com.bolirana.backend.repository;

import com.bolirana.backend.domain.ConfiguracionRiesgo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionRiesgoRepository extends JpaRepository<ConfiguracionRiesgo, Long> {

    /**
     * Busca la configuración de riesgo asociada a un mercado.
     *
     * @param mercadoId identificador del mercado
     * @return Optional con la configuración de riesgo si existe, vacío si no se encuentra
     */
    Optional<ConfiguracionRiesgo> findByMercadoId(Long mercadoId);
}
