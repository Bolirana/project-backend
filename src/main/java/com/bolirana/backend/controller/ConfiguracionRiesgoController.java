package com.bolirana.backend.controller;

import com.bolirana.backend.domain.ConfiguracionRiesgo;
import com.bolirana.backend.service.ConfiguracionRiesgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/riesgo")
@RequiredArgsConstructor
public class ConfiguracionRiesgoController {

    private final ConfiguracionRiesgoService configuracionRiesgoService;

    /**
     * Busca la configuración de riesgo asociada a un mercado.
     *
     * @param id identificador del mercado
     * @return 200 con la configuración de riesgo si existe, 404 si no se encuentra
     */
    @GetMapping("/mercado/{id}")
    public ResponseEntity<ConfiguracionRiesgo> buscarPorMercado(@PathVariable Long id) {
        return configuracionRiesgoService.buscarPorMercado(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Crea o actualiza la configuración de riesgo de un mercado.
     *
     * @param configuracionRiesgo datos de la configuración de riesgo
     * @return 200 con la configuración de riesgo guardada
     */
    @PostMapping
    public ResponseEntity<ConfiguracionRiesgo> crear(@RequestBody ConfiguracionRiesgo configuracionRiesgo) {
        ConfiguracionRiesgo guardado = configuracionRiesgoService.crearOActualizar(configuracionRiesgo);
        return ResponseEntity.ok(guardado);
    }
}
