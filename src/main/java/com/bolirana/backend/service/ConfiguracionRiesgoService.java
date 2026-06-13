package com.bolirana.backend.service;

import com.bolirana.backend.domain.ConfiguracionRiesgo;
import com.bolirana.backend.repository.ConfiguracionRiesgoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ConfiguracionRiesgoService {

    private final ConfiguracionRiesgoRepository configuracionRiesgoRepository;

    /**
     * Busca y retorna la configuración de riesgo asociada a un mercado.
     *
     * @param mercadoId identificador del mercado
     * @return Optional con la configuración de riesgo si existe, vacío si no se encuentra
     */
    public Optional<ConfiguracionRiesgo> buscarPorMercado(Long mercadoId) {
        return configuracionRiesgoRepository.findByMercadoId(mercadoId);
    }

    /**
     * Crea la configuración de riesgo de un mercado o actualiza el límite de
     * alerta si ya existe una configuración para ese mercado.
     *
     * @param configuracionRiesgo datos de la configuración de riesgo
     * @return la configuración de riesgo creada o actualizada
     */
    public ConfiguracionRiesgo crearOActualizar(ConfiguracionRiesgo configuracionRiesgo) {
        Optional<ConfiguracionRiesgo> existente =
                configuracionRiesgoRepository.findByMercadoId(configuracionRiesgo.getMercado().getId());

        if (existente.isPresent()) {
            ConfiguracionRiesgo actual = existente.get();
            actual.setLimiteAlerta(configuracionRiesgo.getLimiteAlerta());
            return configuracionRiesgoRepository.save(actual);
        }

        return configuracionRiesgoRepository.save(configuracionRiesgo);
    }
}
