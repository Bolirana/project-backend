package com.example.eventos.dto;

import com.example.eventos.model.HistorialCambioCuota;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** RF-23: DTO de salida para consultar el historial de cambios de cuota. */
public class HistorialCambioCuotaDTO {

    private Long id;
    private Long opcionApuestaId;
    private String nombreOpcion;
    private BigDecimal cuotaAnterior;
    private BigDecimal cuotaNueva;
    private String origen;
    private LocalDateTime cambiadoEn;

    public static HistorialCambioCuotaDTO desdeEntidad(HistorialCambioCuota historial) {
        HistorialCambioCuotaDTO dto = new HistorialCambioCuotaDTO();
        dto.id = historial.getId();
        dto.opcionApuestaId = historial.getOpcionApuesta().getId();
        dto.nombreOpcion = historial.getOpcionApuesta().getNombre();
        dto.cuotaAnterior = historial.getCuotaAnterior();
        dto.cuotaNueva = historial.getCuotaNueva();
        dto.origen = historial.getOrigen();
        dto.cambiadoEn = historial.getCambiadoEn();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOpcionApuestaId() {
        return opcionApuestaId;
    }

    public void setOpcionApuestaId(Long opcionApuestaId) {
        this.opcionApuestaId = opcionApuestaId;
    }

    public String getNombreOpcion() {
        return nombreOpcion;
    }

    public void setNombreOpcion(String nombreOpcion) {
        this.nombreOpcion = nombreOpcion;
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
