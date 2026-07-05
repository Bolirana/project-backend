package com.bolirana.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Datos de entrada para el registro de un nuevo usuario.
 * La contrasena viaja en texto plano unicamente en este objeto de transporte;
 * se hashea antes de persistirse y nunca se guarda ni se retorna tal cual.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistroUsuarioRequest {

    private String nombreCompleto;
    private String correo;
    private String contrasena;
    private LocalDate fechaNacimiento;
}
