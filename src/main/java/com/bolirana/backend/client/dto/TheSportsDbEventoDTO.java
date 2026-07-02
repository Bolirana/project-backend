package com.bolirana.backend.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * RF-04: DTOs simplificados para mapear la respuesta de TheSportsDB
 * (información de equipos, deporte y fecha de eventos).
 */
public class TheSportsDbEventoDTO {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventosResponse {
        @JsonProperty("events")
        private List<EventoExterno> eventos;

        public List<EventoExterno> getEventos() {
            return eventos;
        }

        public void setEventos(List<EventoExterno> eventos) {
            this.eventos = eventos;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EventoExterno {

        @JsonProperty("idEvent")
        private String idEvento;

        @JsonProperty("strEvent")
        private String nombreEvento;

        @JsonProperty("strHomeTeam")
        private String equipoLocal;

        @JsonProperty("strAwayTeam")
        private String equipoVisitante;

        @JsonProperty("idHomeTeam")
        private String idEquipoLocal;

        @JsonProperty("idAwayTeam")
        private String idEquipoVisitante;

        @JsonProperty("strSport")
        private String deporte;

        @JsonProperty("dateEvent")
        private String fechaEvento;

        @JsonProperty("strTime")
        private String horaEvento;

        public String getIdEvento() {
            return idEvento;
        }

        public void setIdEvento(String idEvento) {
            this.idEvento = idEvento;
        }

        public String getNombreEvento() {
            return nombreEvento;
        }

        public void setNombreEvento(String nombreEvento) {
            this.nombreEvento = nombreEvento;
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

        public String getIdEquipoLocal() {
            return idEquipoLocal;
        }

        public void setIdEquipoLocal(String idEquipoLocal) {
            this.idEquipoLocal = idEquipoLocal;
        }

        public String getIdEquipoVisitante() {
            return idEquipoVisitante;
        }

        public void setIdEquipoVisitante(String idEquipoVisitante) {
            this.idEquipoVisitante = idEquipoVisitante;
        }

        public String getDeporte() {
            return deporte;
        }

        public void setDeporte(String deporte) {
            this.deporte = deporte;
        }

        public String getFechaEvento() {
            return fechaEvento;
        }

        public void setFechaEvento(String fechaEvento) {
            this.fechaEvento = fechaEvento;
        }

        public String getHoraEvento() {
            return horaEvento;
        }

        public void setHoraEvento(String horaEvento) {
            this.horaEvento = horaEvento;
        }
    }
}
