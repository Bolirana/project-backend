package com.example.eventos.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class OpcionApuestaDTO {

    @NotBlank(message = "El nombre de la opción de apuesta es obligatorio")
    private String nombre;

    /**
     * Cuota de referencia obtenida desde The Odds API (opcional, solo guía
     * para el Administrador). NO se persiste en la base de datos.
     */
    @DecimalMin(value = "1.0", inclusive = false, message = "La cuota de referencia debe ser mayor a 1.0")
    private BigDecimal cuotaReferencia;

    /**
     * RF-18: cuota actual/final definida manualmente por el Administrador.
     * Siempre debe ser estrictamente mayor a 1.0. Se persiste en
     * opcion_apuesta.cuota_actual.
     */
    @NotNull(message = "La cuota actual es obligatoria")
    @DecimalMin(value = "1.0", inclusive = false, message = "La cuota actual debe ser mayor a 1.0 (RF-18)")
    private BigDecimal cuotaActual;

    public OpcionApuestaDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getCuotaReferencia() {
        return cuotaReferencia;
    }

    public void setCuotaReferencia(BigDecimal cuotaReferencia) {
        this.cuotaReferencia = cuotaReferencia;
    }

    public BigDecimal getCuotaActual() {
        return cuotaActual;
    }

    public void setCuotaActual(BigDecimal cuotaActual) {
        this.cuotaActual = cuotaActual;
    }
}
