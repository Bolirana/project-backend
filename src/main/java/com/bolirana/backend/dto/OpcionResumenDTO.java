package com.bolirana.backend.dto;

import com.bolirana.backend.domain.OpcionApuesta;

import java.math.BigDecimal;

/** Resumen de una opción de apuesta con su mercado anidado, usado por ApuestaRespuestaDTO. */
public record OpcionResumenDTO(Long id, String nombre, BigDecimal cuotaActual, MercadoResumenDTO mercado) {

    public static OpcionResumenDTO desdeEntidad(OpcionApuesta opcion) {
        return new OpcionResumenDTO(
                opcion.getId(),
                opcion.getNombre(),
                opcion.getCuotaActual(),
                MercadoResumenDTO.desdeEntidad(opcion.getMercado()));
    }
}
