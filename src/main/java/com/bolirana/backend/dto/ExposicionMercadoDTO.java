package com.bolirana.backend.dto;

import java.util.List;

/** Exposición de riesgo agrupada por mercado, con la exposición de cada una de sus opciones. */
public record ExposicionMercadoDTO(Long mercadoId, String nombreMercado, List<ExposicionOpcionDTO> opciones) {
}
