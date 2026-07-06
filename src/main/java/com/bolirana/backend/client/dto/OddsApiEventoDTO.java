package com.bolirana.backend.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * RF-04: DTOs simplificados para mapear la respuesta de The Odds API
 * (cuotas de referencia del mercado real).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OddsApiEventoDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("sport_key")
    private String sportKey;

    @JsonProperty("home_team")
    private String equipoLocal;

    @JsonProperty("away_team")
    private String equipoVisitante;

    @JsonProperty("commence_time")
    private String fechaInicio;

    @JsonProperty("bookmakers")
    private List<Bookmaker> casasDeApuestas;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSportKey() {
        return sportKey;
    }

    public void setSportKey(String sportKey) {
        this.sportKey = sportKey;
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

    public String getFechaInicio() {
        return fechaInicio;
    }

    public void setFechaInicio(String fechaInicio) {
        this.fechaInicio = fechaInicio;
    }

    public List<Bookmaker> getCasasDeApuestas() {
        return casasDeApuestas;
    }

    public void setCasasDeApuestas(List<Bookmaker> casasDeApuestas) {
        this.casasDeApuestas = casasDeApuestas;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Bookmaker {
        @JsonProperty("key")
        private String key;

        @JsonProperty("title")
        private String titulo;

        @JsonProperty("markets")
        private List<Market> mercados;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String getTitulo() {
            return titulo;
        }

        public void setTitulo(String titulo) {
            this.titulo = titulo;
        }

        public List<Market> getMercados() {
            return mercados;
        }

        public void setMercados(List<Market> mercados) {
            this.mercados = mercados;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Market {
        @JsonProperty("key")
        private String key;

        @JsonProperty("outcomes")
        private List<Outcome> opciones;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public List<Outcome> getOpciones() {
            return opciones;
        }

        public void setOpciones(List<Outcome> opciones) {
            this.opciones = opciones;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Outcome {
        @JsonProperty("name")
        private String nombre;

        @JsonProperty("price")
        private Double precio;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Double getPrecio() {
            return precio;
        }

        public void setPrecio(Double precio) {
            this.precio = precio;
        }
    }
}
