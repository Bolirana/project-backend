package com.example.eventos.exception;

/** RF-16: el sistema no debe permitir transiciones de estado inválidas. */
public class TransicionEstadoInvalidaException extends RuntimeException {
    public TransicionEstadoInvalidaException(String message) {
        super(message);
    }
}
