package com.bolirana.backend.dto;

public record RecargaRequest(Long usuarioId, Double monto, String metodoPago) {
}
