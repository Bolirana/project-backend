package com.bolirana.backend.dto;

import java.math.BigDecimal;

/** RF-12/RF-13: exposición económica de una opción de apuesta y su estado de alerta de riesgo. */
public record ExposicionOpcionDTO(
        Long opcionId,
        String nombreOpcion,
        BigDecimal cuotaActual,
        Double exposicion,
        Double limiteAlerta,
        boolean alerta,
        SugerenciaCuotaDTO sugerencia) {
}
