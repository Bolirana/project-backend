package com.bolirana.backend.dto;

import java.math.BigDecimal;

/** RF-14: sugerencia de ajuste de cuota para una opción cuya exposición superó el límite de riesgo. */
public record SugerenciaCuotaDTO(BigDecimal cuotaActual, BigDecimal cuotaSugerida, String razon) {
}
