package com.bolirana.backend.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "historial_cuota")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HistorialCuota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "opcion_id", nullable = false)
    private OpcionApuesta opcion;

    @Column(name = "cuota_anterior")
    private Double cuotaAnterior;

    @Column(name = "cuota_nueva")
    private Double cuotaNueva;

    @Column(name = "origen")
    private String origen;

    @CreationTimestamp
    @Column(name = "cambiado_en", updatable = false)
    private LocalDateTime cambiadoEn;
}
