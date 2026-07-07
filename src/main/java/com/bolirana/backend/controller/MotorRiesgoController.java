package com.bolirana.backend.controller;

import com.bolirana.backend.dto.ExposicionEventoDTO;
import com.bolirana.backend.service.MotorRiesgoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/riesgo")
@RequiredArgsConstructor
public class MotorRiesgoController {

    private final MotorRiesgoService motorRiesgoService;

    /** RF-12/RF-13/RF-14: exposición económica, alertas y sugerencias de cuota por opción del evento. */
    @GetMapping("/exposicion/{eventoId}")
    public ExposicionEventoDTO calcularExposicion(@PathVariable Long eventoId) {
        return motorRiesgoService.calcularExposicion(eventoId);
    }
}
