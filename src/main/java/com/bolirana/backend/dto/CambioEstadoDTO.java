package com.bolirana.backend.dto;

import com.bolirana.backend.enums.EstadoEvento;
import jakarta.validation.constraints.NotNull;

/** RF-05 / RF-16: DTO para solicitar el cambio de estado de un evento. */
public class CambioEstadoDTO {

    @NotNull(message = "El nuevo estado es obligatorio")
    private EstadoEvento nuevoEstado;

    public CambioEstadoDTO() {
    }

    public EstadoEvento getNuevoEstado() {
        return nuevoEstado;
    }

    public void setNuevoEstado(EstadoEvento nuevoEstado) {
        this.nuevoEstado = nuevoEstado;
    }
}
