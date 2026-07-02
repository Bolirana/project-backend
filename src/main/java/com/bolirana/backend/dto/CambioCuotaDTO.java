package com.example.eventos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** RF-23: DTO para solicitar el cambio de cuota final de una opción de apuesta. */
public class CambioCuotaDTO {

    @NotNull(message = "La nueva cuota es obligatoria")
    @DecimalMin(value = "1.0", inclusive = false, message = "La cuota debe ser mayor a 1.0 (RF-18)")
    private BigDecimal nuevaCuota;

    public CambioCuotaDTO() {
    }

    public BigDecimal getNuevaCuota() {
        return nuevaCuota;
    }

    public void setNuevaCuota(BigDecimal nuevaCuota) {
        this.nuevaCuota = nuevaCuota;
    }
}
