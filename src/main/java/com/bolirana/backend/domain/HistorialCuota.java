package com.example.eventos.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * RF-23: Cada cambio de cuota debe quedar registrado indicando
 * la cuota anterior y la nueva, para poder consultarlo después.
 *
 * Mapeo alineado a la tabla `historial_cuota` de la base de datos del grupo,
 * que incluye además el campo `origen` (MANUAL | SUGERENCIA_MOTOR_RIESGO).
 * Para los cambios realizados por el Administrador vía RF-23, el origen
 * siempre será MANUAL.
 */
@Entity
@Table(name = "historial_cuota")
public class HistorialCambioCuota {

    public static final String ORIGEN_MANUAL = "MANUAL";
    public static final String ORIGEN_SUGERENCIA_MOTOR_RIESGO = "SUGERENCIA_MOTOR_RIESGO";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "opcion_id")
    private OpcionApuesta opcionApuesta;

    @Column(name = "cuota_anterior")
    private BigDecimal cuotaAnterior;

    @Column(name = "cuota_nueva")
    private BigDecimal cuotaNueva;

    /** MANUAL | SUGERENCIA_MOTOR_RIESGO. RF-23 siempre registra MANUAL. */
    @Column(name = "origen")
    private String origen;

    @Column(name = "cambiado_en")
    private LocalDateTime cambiadoEn;

    public HistorialCambioCuota() {
        this.cambiadoEn = LocalDateTime.now();
    }

    public HistorialCambioCuota(OpcionApuesta opcionApuesta, BigDecimal cuotaAnterior, BigDecimal cuotaNueva, String origen) {
        this.opcionApuesta = opcionApuesta;
        this.cuotaAnterior = cuotaAnterior;
        this.cuotaNueva = cuotaNueva;
        this.origen = origen;
        this.cambiadoEn = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OpcionApuesta getOpcionApuesta() {
        return opcionApuesta;
    }

    public void setOpcionApuesta(OpcionApuesta opcionApuesta) {
        this.opcionApuesta = opcionApuesta;
    }

    public BigDecimal getCuotaAnterior() {
        return cuotaAnterior;
    }

    public void setCuotaAnterior(BigDecimal cuotaAnterior) {
        this.cuotaAnterior = cuotaAnterior;
    }

    public BigDecimal getCuotaNueva() {
        return cuotaNueva;
    }

    public void setCuotaNueva(BigDecimal cuotaNueva) {
        this.cuotaNueva = cuotaNueva;
    }

    public String getOrigen() {
        return origen;
    }

    public void setOrigen(String origen) {
        this.origen = origen;
    }

    public LocalDateTime getCambiadoEn() {
        return cambiadoEn;
    }

    public void setCambiadoEn(LocalDateTime cambiadoEn) {
        this.cambiadoEn = cambiadoEn;
    }
}
