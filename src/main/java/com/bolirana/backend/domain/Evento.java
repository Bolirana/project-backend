package com.bolirana.backend.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import com.bolirana.backend.enums.EstadoEvento;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "evento")
@Data
//@NoArgsConstructor
@AllArgsConstructor
public class Evento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "deporte")
    private String deporte;

    @Column(name = "fecha_evento")
    private LocalDate fechaEvento;

    @Column(name = "equipo_local")
    private String equipoLocal;

    /** NUEVA COLUMNA PROPUESTA: equipo_visitante (varchar, nullable) - ver nota de clase. */
    @Column(name = "equipo_visitante")
    private String equipoVisitante;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado")
    private EstadoEvento estado;

    @CreationTimestamp
    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @OneToMany(mappedBy = "evento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Mercado> mercados = new ArrayList<>();    

    @Transient
    private String idEquipoLocalExterno;
    @Transient
    private String idEquipoVisitanteExterno;
    @Transient
    private String idEventoExternoOdds;

    @Transient
    private LocalDateTime fechaActualizacion;

    public Evento() {
        this.creadoEn = LocalDateTime.now();
        this.estado = EstadoEvento.CREADO;
    }

    /** Genera el campo `nombre` a partir de los equipos, ej: "Equipo A vs Equipo B". */
    public void actualizarNombre() {
        if (equipoLocal != null && equipoVisitante != null) {
            this.nombre = equipoLocal + " vs " + equipoVisitante;
        }
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

    public EstadoEvento getEstado() {
        return estado;
    }

    public void setEstado(EstadoEvento estado) {
        this.estado = estado;
    }

    public List<Mercado> getMercados() {
        return mercados;
    }

    public void setMercados(List<Mercado> mercados) {
        this.mercados = mercados;
    }

    public void agregarMercado(Mercado mercado) {
        mercado.setEvento(this);
        this.mercados.add(mercado);
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getFechaActualizacion() {
        return fechaActualizacion;
    }

    public void setFechaActualizacion(LocalDateTime fechaActualizacion) {
        this.fechaActualizacion = fechaActualizacion;
    }
}
