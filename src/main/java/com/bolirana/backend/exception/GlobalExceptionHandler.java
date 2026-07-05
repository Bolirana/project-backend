package com.bolirana.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Traduce las excepciones de negocio del flujo de autenticacion a codigos HTTP
 * apropiados. No intercepta excepciones de otros modulos (se mantienen con su
 * comportamiento actual) para no alterar endpoints ya existentes.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Maneja el intento de registro con un correo ya existente.
     *
     * @param ex excepcion capturada
     * @return 409 Conflict con un mensaje descriptivo
     */
    @ExceptionHandler(CorreoDuplicadoException.class)
    public ResponseEntity<Map<String, String>> manejarCorreoDuplicado(CorreoDuplicadoException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("mensaje", ex.getMessage()));
    }

    /**
     * Maneja credenciales incorrectas durante el login.
     *
     * @param ex excepcion capturada
     * @return 401 Unauthorized con un mensaje descriptivo
     */
    @ExceptionHandler(CredencialesInvalidasException.class)
    public ResponseEntity<Map<String, String>> manejarCredencialesInvalidas(CredencialesInvalidasException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("mensaje", ex.getMessage()));
    }

    /**
     * Maneja el intento de login de una cuenta suspendida o eliminada.
     *
     * @param ex excepcion capturada
     * @return 403 Forbidden con un mensaje descriptivo
     */
    @ExceptionHandler(CuentaSuspendidaException.class)
    public ResponseEntity<Map<String, String>> manejarCuentaSuspendida(CuentaSuspendidaException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("mensaje", ex.getMessage()));
    }
}
