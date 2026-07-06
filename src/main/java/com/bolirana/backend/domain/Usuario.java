package com.bolirana.backend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_completo")
    private String nombreCompleto;

    @Column(name = "correo", nullable = false, unique = true)
    private String correo;

    // @JsonIgnore: el hash nunca debe salir en una respuesta HTTP,
    // ni siquiera hasheado (requisito de no exposicion de credenciales).
    @JsonIgnore
    @Column(name = "contrasena_hash", nullable = false)
    private String contrasenaHash;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Enumerated(EnumType.STRING)
    @Column(name = "rol")
    private RolUsuario rol;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoUsuario estado;

    @Column(name = "saldo")
    private Double saldo = 0.0;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;
}
