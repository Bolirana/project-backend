package com.bolirana.backend.exception;

/** RF-04: validaciones de negocio, ej: el evento debe tener al menos un mercado con opciones. */
public class ValidacionNegocioException extends RuntimeException {
    public ValidacionNegocioException(String message) {
        super(message);
    }
}
