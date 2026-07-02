package com.example.eventos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Future;

import java.time.LocalDate;
import java.util.List;

/**
 * RF-04: DTO para la creación de un evento deportivo.
 * Incluye datos importados desde TheSportsDB (equipos, deporte, fecha)
 * y la lista de mercados con sus cuotas (al menos uno es obligatorio).
 */
public class EventoCreacionDTO {

    @NotBlank(message = "El equipo local es obligatorio")
    private String equipoLocal;

    @NotBlank(message = "El equipo visitante es obligatorio")
    private String equipoVisitante;

    @NotBlank(message = "El deporte es obligatorio")
    private String deporte;

    @NotNull(message = "La fecha del evento es obligatoria")
    @Future(message = "La fecha del evento debe ser futura")
    private LocalDate fechaEvento;

    // Identificadores externos opcionales para trazabilidad de las APIs
    private String idEquipoLocalExterno;
    private String idEquipoVisitanteExterno;
    private String idEventoExternoOdds;

    @NotEmpty(message = "El evento debe tener al menos un mercado con sus opciones de apuesta")
    @Valid
    private List<MercadoDTO> mercados;

    public EventoCreacionDTO() {
    }

    public String getEquipoLocal() {
        return equipoLocal;
    }

    public void setEquipoLocal(String equipoLocal) {
        this.equipoLocal = equipoLocal;
    }

    public String getEquipoVisitante() {
        return equipoVisitante;
    }

    public void setEquipoVisitante(String equipoVisitante) {
        this.equipoVisitante = equipoVisitante;
    }

    public String getDeporte() {
        return deporte;
    }

    public void setDeporte(String deporte) {
        this.deporte = deporte;
    }

    public LocalDate getFechaEvento() {
        return fechaEvento;
    }

    public void setFechaEvento(LocalDate fechaEvento) {
        this.fechaEvento = fechaEvento;
    }

    public String getIdEquipoLocalExterno() {
        return idEquipoLocalExterno;
    }

    public void setIdEquipoLocalExterno(String idEquipoLocalExterno) {
        this.idEquipoLocalExterno = idEquipoLocalExterno;
    }

    public String getIdEquipoVisitanteExterno() {
        return idEquipoVisitanteExterno;
    }

    public void setIdEquipoVisitanteExterno(String idEquipoVisitanteExterno) {
        this.idEquipoVisitanteExterno = idEquipoVisitanteExterno;
    }

    public String getIdEventoExternoOdds() {
        return idEventoExternoOdds;
    }

    public void setIdEventoExternoOdds(String idEventoExternoOdds) {
        this.idEventoExternoOdds = idEventoExternoOdds;
    }

    public List<MercadoDTO> getMercados() {
        return mercados;
    }

    public void setMercados(List<MercadoDTO> mercados) {
        this.mercados = mercados;
    }
}
