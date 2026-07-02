package com.example.eventos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class MercadoDTO {

    @NotBlank(message = "El nombre del mercado es obligatorio")
    private String nombre;

    @NotEmpty(message = "El mercado debe tener al menos una opción de apuesta")
    @Valid
    private List<OpcionApuestaDTO> opciones;

    public MercadoDTO() {
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<OpcionApuestaDTO> getOpciones() {
        return opciones;
    }

    public void setOpciones(List<OpcionApuestaDTO> opciones) {
        this.opciones = opciones;
    }
}
