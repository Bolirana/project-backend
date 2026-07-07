package com.bolirana.backend.dto;

import com.bolirana.backend.domain.Evento;
import com.bolirana.backend.enums.EstadoEvento;

import java.time.LocalDate;

/**
 * Resumen de un evento sin su lista de mercados, usado como referencia
 * "hacia arriba" desde Mercado/OpcionApuesta/Apuesta. Evita el ciclo
 * Evento&lt;-&gt;Mercado&lt;-&gt;OpcionApuesta que producen las entidades JPA.
 */
public record EventoResumenDTO(
        Long id,
        String nombre,
        String deporte,
        LocalDate fechaEvento,
        String equipoLocal,
        String equipoVisitante,
        EstadoEvento estado) {

    public static EventoResumenDTO desdeEntidad(Evento evento) {
        return new EventoResumenDTO(
                evento.getId(),
                evento.getNombre(),
                evento.getDeporte(),
                evento.getFechaEvento(),
                evento.getEquipoLocal(),
                evento.getEquipoVisitante(),
                evento.getEstado());
    }
}
