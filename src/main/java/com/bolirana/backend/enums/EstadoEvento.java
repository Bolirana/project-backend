package com.bolirana.backend.enums;

/**
 * RF-05: Un evento debe pasar por los estados CREADO, ABIERTO,
 * CERRADO, LIQUIDADO y CANCELADO en ese orden.
 */
public enum EstadoEvento {
    CREADO,
    ABIERTO,
    CERRADO,
    LIQUIDADO,
    CANCELADO;

    /**
     * Valida si una transición de estado es permitida según el flujo
     * CREADO -> ABIERTO -> CERRADO -> LIQUIDADO,
     * con CANCELADO posible desde CREADO o ABIERTO.
     * RF-16: no permitir reabrir un evento liquidado ni cancelar uno ya liquidado.
     */
    public boolean puedeTransicionarA(EstadoEvento nuevoEstado) {
        switch (this) {
            case CREADO:
                return nuevoEstado == ABIERTO || nuevoEstado == CANCELADO;
            case ABIERTO:
                return nuevoEstado == CERRADO || nuevoEstado == CANCELADO;
            case CERRADO:
                return nuevoEstado == LIQUIDADO;
            case LIQUIDADO:
                return false; // RF-16: no se puede reabrir ni cancelar un evento liquidado
            case CANCELADO:
                return false; // estado final
            default:
                return false;
        }
    }
}
