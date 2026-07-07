package com.bolirana.backend.dto;

import java.util.List;

/** Respuesta de GET /api/riesgo/exposicion/{eventoId} (RF-12/RF-13/RF-14). */
public record ExposicionEventoDTO(Long eventoId, String nombreEvento, List<ExposicionMercadoDTO> mercados) {
}
