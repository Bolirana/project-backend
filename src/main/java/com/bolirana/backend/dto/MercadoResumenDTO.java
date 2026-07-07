package com.bolirana.backend.dto;

import com.bolirana.backend.domain.Mercado;

/**
 * Resumen de un mercado con su evento anidado (sin la lista de opciones,
 * para no reabrir el ciclo Mercado&lt;-&gt;OpcionApuesta). Usado tanto por
 * GET /api/mercados como por ApuestaRespuestaDTO.
 */
public record MercadoResumenDTO(Long id, String nombre, EventoResumenDTO evento) {

    public static MercadoResumenDTO desdeEntidad(Mercado mercado) {
        return new MercadoResumenDTO(
                mercado.getId(),
                mercado.getNombre(),
                EventoResumenDTO.desdeEntidad(mercado.getEvento()));
    }
}
