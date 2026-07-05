package com.bolirana.backend.exception;

/**
 * Se lanza cuando el correo no existe o la contrasena no coincide durante el login.
 * El mensaje es deliberadamente generico para no revelar si el correo existe o no.
 */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("Correo o contrasena incorrectos");
    }
}
