package com.bolirana.backend.domain;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "opcion_apuesta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class OpcionApuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mercado_id", nullable = false)
    private Mercado mercado;

    @Column(name = "nombre")
    private String nombre;

    @Column(name = "cuota_actual")
    private BigDecimal cuotaActual;


    public OpcionApuesta(String nombre, BigDecimal cuotaActual) {
        this.nombre = nombre;
        this.cuotaActual = cuotaActual;
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

    public Mercado getMercado() {
        return mercado;
    }

    public void setMercado(Mercado mercado) {
        this.mercado = mercado;
    }
}
