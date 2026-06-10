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

    public Optional<ConfiguracionRiesgo> buscarPorMercado(Long mercadoId) {
        return configuracionRiesgoRepository.findByMercadoId(mercadoId);
    }

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
