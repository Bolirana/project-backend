package com.bolirana.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Datos de entrada para el inicio de sesion de un usuario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    private String correo;
    private String contrasena;
}
