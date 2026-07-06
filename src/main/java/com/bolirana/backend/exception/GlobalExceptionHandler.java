package com.bolirana.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Traduce las excepciones de negocio de los distintos modulos a codigos HTTP
 * apropiados.
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

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Map<String, Object>> handleNoEncontrado(RecursoNoEncontradoException ex) {
        return construirRespuesta(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(TransicionEstadoInvalidaException.class)
    public ResponseEntity<Map<String, Object>> handleTransicionInvalida(TransicionEstadoInvalidaException ex) {
        return construirRespuesta(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(ValidacionNegocioException.class)
    public ResponseEntity<Map<String, Object>> handleValidacionNegocio(ValidacionNegocioException ex) {
        return construirRespuesta(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidacion(MethodArgumentNotValidException ex) {
        StringBuilder sb = new StringBuilder("Errores de validación: ");
        ex.getBindingResult().getFieldErrors().forEach(err ->
                sb.append(err.getField()).append(" - ").append(err.getDefaultMessage()).append("; "));
        return construirRespuesta(HttpStatus.BAD_REQUEST, sb.toString());
    }

    private ResponseEntity<Map<String, Object>> construirRespuesta(HttpStatus status, String mensaje) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("mensaje", mensaje);
        return new ResponseEntity<>(body, status);
    }
}
