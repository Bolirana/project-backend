package com.bolirana.backend.service;

import com.bolirana.backend.domain.ConfiguracionRiesgo;
import com.bolirana.backend.domain.Mercado;
import com.bolirana.backend.repository.ConfiguracionRiesgoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfiguracionRiesgoServiceTest {

    @Mock
    private ConfiguracionRiesgoRepository configuracionRiesgoRepository;

    @InjectMocks
    private ConfiguracionRiesgoService configuracionRiesgoService;

    @Test
    @DisplayName("crearOActualizar() actualiza limiteAlerta cuando ya existe config para el mercado")
    void crearOActualizar_configExistente_actualizaLimiteAlerta() {
        // Caso límite: el mercado ya tiene config, por lo que solo debe modificarse limiteAlerta sin crear un nuevo registro
        Mercado mercado = new Mercado();
        mercado.setId(1L);

        ConfiguracionRiesgo existente = new ConfiguracionRiesgo();
        existente.setId(10L);
        existente.setMercado(mercado);
        existente.setLimiteAlerta(500000.0);

        ConfiguracionRiesgo entrante = new ConfiguracionRiesgo();
        entrante.setMercado(mercado);
        entrante.setLimiteAlerta(750000.0);

        when(configuracionRiesgoRepository.findByMercadoId(1L)).thenReturn(Optional.of(existente));
        when(configuracionRiesgoRepository.save(existente)).thenReturn(existente);

        ConfiguracionRiesgo resultado = configuracionRiesgoService.crearOActualizar(entrante);

        assertThat(resultado.getLimiteAlerta()).isEqualTo(750000.0);
        verify(configuracionRiesgoRepository, times(1)).save(existente);
    }

    @Test
    @DisplayName("crearOActualizar() crea registro nuevo cuando no existe config para el mercado")
    void crearOActualizar_configInexistente_creaRegistroNuevo() {
        // Caso límite: el mercado no tiene config previa, por lo que la configuración entrante debe persistirse como nuevo registro
        Mercado mercado = new Mercado();
        mercado.setId(1L);

        ConfiguracionRiesgo entrante = new ConfiguracionRiesgo();
        entrante.setMercado(mercado);
        entrante.setLimiteAlerta(750000.0);

        when(configuracionRiesgoRepository.findByMercadoId(1L)).thenReturn(Optional.empty());
        when(configuracionRiesgoRepository.save(entrante)).thenReturn(entrante);

        ConfiguracionRiesgo resultado = configuracionRiesgoService.crearOActualizar(entrante);

        assertThat(resultado).isSameAs(entrante);
        verify(configuracionRiesgoRepository, times(1)).save(entrante);
    }

    @Test
    @DisplayName("crearOActualizar() guarda un objeto cuyo mercadoId es el correcto (1L)")
    void crearOActualizar_objetoGuardado_tieneMercadoIdCorrecto() {
        // Caso límite: se inspecciona el argumento real de save() para garantizar que el mercadoId no fue alterado por el servicio
        Mercado mercado = new Mercado();
        mercado.setId(1L);

        ConfiguracionRiesgo entrante = new ConfiguracionRiesgo();
        entrante.setMercado(mercado);
        entrante.setLimiteAlerta(300000.0);

        when(configuracionRiesgoRepository.findByMercadoId(1L)).thenReturn(Optional.empty());
        when(configuracionRiesgoRepository.save(entrante)).thenReturn(entrante);

        configuracionRiesgoService.crearOActualizar(entrante);

        verify(configuracionRiesgoRepository).save(
                argThat(config -> config.getMercado().getId().equals(1L))
        );
    }
}
