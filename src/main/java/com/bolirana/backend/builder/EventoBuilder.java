package com.example.eventos.builder;

import com.example.eventos.model.Evento;
import com.example.eventos.model.Mercado;
import com.example.eventos.model.OpcionApuesta;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * RNF-02: La creación de eventos con múltiples mercados se implementa
 * usando el patrón de diseño Builder, de forma que se puedan agregar
 * opciones paso a paso sin necesitar un constructor con muchos parámetros.
 *
 * Uso típico:
 * Evento evento = new EventoBuilder()
 *      .conEquipoLocal("Equipo A")
 *      .conEquipoVisitante("Equipo B")
 *      .conDeporte("Fútbol")
 *      .conFechaEvento(fecha)
 *      .agregarMercado(new MercadoBuilder("Resultado del partido")
 *              .agregarOpcion("Local gana", refLocal, finalLocal)
 *              .agregarOpcion("Empate", refEmpate, finalEmpate)
 *              .agregarOpcion("Visitante gana", refVisitante, finalVisitante)
 *              .build())
 *      .build();
 */
public class EventoBuilder {

    private final Evento evento;

    public EventoBuilder() {
        this.evento = new Evento();
    }

    public EventoBuilder conEquipoLocal(String equipoLocal) {
        evento.setEquipoLocal(equipoLocal);
        return this;
    }

    public EventoBuilder conEquipoVisitante(String equipoVisitante) {
        evento.setEquipoVisitante(equipoVisitante);
        return this;
    }

    public EventoBuilder conDeporte(String deporte) {
        evento.setDeporte(deporte);
        return this;
    }

    public EventoBuilder conFechaEvento(LocalDate fechaEvento) {
        evento.setFechaEvento(fechaEvento);
        return this;
    }

    /** Identificador externo de TheSportsDB para el equipo local (no persistido, ver entidad Evento). */
    public EventoBuilder conIdEquipoLocalExterno(String id) {
        evento.setIdEquipoLocalExterno(id);
        return this;
    }

    /** Identificador externo de TheSportsDB para el equipo visitante (no persistido). */
    public EventoBuilder conIdEquipoVisitanteExterno(String id) {
        evento.setIdEquipoVisitanteExterno(id);
        return this;
    }

    /** Identificador externo de The Odds API (no persistido). */
    public EventoBuilder conIdEventoExternoOdds(String id) {
        evento.setIdEventoExternoOdds(id);
        return this;
    }

    /** Agrega un mercado ya construido (ver {@link MercadoBuilder}) al evento. */
    public EventoBuilder agregarMercado(Mercado mercado) {
        evento.agregarMercado(mercado);
        return this;
    }

    /**
     * Construye el evento final. Genera automáticamente el campo `nombre`
     * (columna existente en la BD) a partir de equipoLocal/equipoVisitante,
     * ej: "Equipo A vs Equipo B".
     */
    public Evento build() {
        evento.actualizarNombre();
        return evento;
    }

    /**
     * Builder anidado para construir mercados paso a paso, agregando
     * opciones de apuesta de a una sin requerir un constructor con
     * muchos parámetros.
     */
    public static class MercadoBuilder {

        private final Mercado mercado;

        public MercadoBuilder(String nombre) {
            this.mercado = new Mercado(nombre);
        }

        /**
         * Agrega una opción de apuesta. La cuotaReferencia (de The Odds API)
         * solo se usa como guía y NO se persiste; cuotaActual (RF-18, > 1.0)
         * es la que se guarda en opcion_apuesta.cuota_actual.
         */
        public MercadoBuilder agregarOpcion(String nombre, BigDecimal cuotaReferencia, BigDecimal cuotaActual) {
            OpcionApuesta opcion = new OpcionApuesta(nombre, cuotaActual);
            mercado.agregarOpcion(opcion);
            return this;
        }

        public MercadoBuilder agregarOpcion(OpcionApuesta opcion) {
            mercado.agregarOpcion(opcion);
            return this;
        }

        public Mercado build() {
            return mercado;
        }
    }
}
