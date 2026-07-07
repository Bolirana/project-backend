package com.bolirana.backend.dto;

import com.bolirana.backend.domain.MovimientoSaldo;

import java.util.List;

/** Forma de respuesta de RF-22, con las apuestas ya convertidas a ApuestaRespuestaDTO. */
public record HistorialApostadorRespuestaDTO(
        Double saldo, List<ApuestaRespuestaDTO> apuestas, List<MovimientoSaldo> movimientos) {
}
