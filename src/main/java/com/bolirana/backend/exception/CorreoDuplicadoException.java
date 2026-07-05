package com.bolirana.backend.exception;

/**
 * Se lanza al intentar registrar un usuario con un correo que ya existe en el sistema.
 */
public class CorreoDuplicadoException extends RuntimeException {

    public CorreoDuplicadoException(String correo) {
        super("El correo '" + correo + "' ya esta registrado en el sistema");
    }
}
