package com.bolirana.backend.exception;

/**
 * Se lanza cuando un usuario con cuenta SUSPENDIDA o ELIMINADA intenta iniciar sesion.
 */
public class CuentaSuspendidaException extends RuntimeException {

    public CuentaSuspendidaException() {
        super("La cuenta esta suspendida o inactiva. Contacte a un administrador");
    }
}
