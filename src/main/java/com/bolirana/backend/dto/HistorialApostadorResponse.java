package com.bolirana.backend.dto;

import com.bolirana.backend.domain.Apuesta;
import com.bolirana.backend.domain.MovimientoSaldo;

import java.util.List;

public record HistorialApostadorResponse(Double saldo, List<Apuesta> apuestas, List<MovimientoSaldo> movimientos) {
}
