package com.bolirana.backend.dto;

import com.bolirana.backend.enums.EstadoEvento;
import com.bolirana.backend.model.Evento;
import com.bolirana.backend.model.Mercado;
import com.bolirana.backend.model.OpcionApuesta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/** DTO de salida con la información completa de un evento, sus mercados y opciones. */
public class EventoRespuestaDTO {

    private Long id;
    private String nombre;
    private String equipoLocal;
    private String equipoVisitante;
    private String deporte;
    private LocalDate fechaEvento;
    private EstadoEvento estado;
    private List<MercadoRespuestaDTO> mercados;
    private LocalDateTime creadoEn;

    public static EventoRespuestaDTO desdeEntidad(Evento evento) {
        EventoRespuestaDTO dto = new EventoRespuestaDTO();
        dto.id = evento.getId();
        dto.nombre = evento.getNombre();
        dto.equipoLocal = evento.getEquipoLocal();
        dto.equipoVisitante = evento.getEquipoVisitante();
        dto.deporte = evento.getDeporte();
        dto.fechaEvento = evento.getFechaEvento();
        dto.estado = evento.getEstado();
        dto.creadoEn = evento.getCreadoEn();
        dto.mercados = evento.getMercados().stream()
                .map(MercadoRespuestaDTO::desdeEntidad)
                .collect(Collectors.toList());
        return dto;
    }

    // Getters y setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public List<MercadoRespuestaDTO> getMercados() {
        return mercados;
    }

    public void setMercados(List<MercadoRespuestaDTO> mercados) {
        this.mercados = mercados;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    /** DTO anidado de respuesta para Mercado */
    public static class MercadoRespuestaDTO {
        private Long id;
        private String nombre;
        private List<OpcionApuestaRespuestaDTO> opciones;

        public static MercadoRespuestaDTO desdeEntidad(Mercado mercado) {
            MercadoRespuestaDTO dto = new MercadoRespuestaDTO();
            dto.id = mercado.getId();
            dto.nombre = mercado.getNombre();
            dto.opciones = mercado.getOpciones().stream()
                    .map(OpcionApuestaRespuestaDTO::desdeEntidad)
                    .collect(Collectors.toList());
            return dto;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public List<OpcionApuestaRespuestaDTO> getOpciones() {
            return opciones;
        }

        public void setOpciones(List<OpcionApuestaRespuestaDTO> opciones) {
            this.opciones = opciones;
        }
    }

    /** DTO anidado de respuesta para OpcionApuesta */
    public static class OpcionApuestaRespuestaDTO {
        private Long id;
        private String nombre;
        private BigDecimal cuotaActual;

        public static OpcionApuestaRespuestaDTO desdeEntidad(OpcionApuesta opcion) {
            OpcionApuestaRespuestaDTO dto = new OpcionApuestaRespuestaDTO();
            dto.id = opcion.getId();
            dto.nombre = opcion.getNombre();
            dto.cuotaActual = opcion.getCuotaActual();
            return dto;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public BigDecimal getCuotaActual() {
            return cuotaActual;
        }

        public void setCuotaActual(BigDecimal cuotaActual) {
            this.cuotaActual = cuotaActual;
        }
    }
}
