package com.bolirana.backend.domain;

/**
 * Estados posibles de la cuenta de un usuario.
 * Un usuario SUSPENDIDO o ELIMINADO no puede iniciar sesion.
 */
public enum EstadoUsuario {
    ACTIVO,
    SUSPENDIDO,
    ELIMINADO
}
