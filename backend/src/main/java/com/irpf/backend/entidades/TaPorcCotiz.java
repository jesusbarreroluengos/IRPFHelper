package com.irpf.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de TaPorcCotiz.
 */
@Entity
@Table(name = "ta_porc_cotiz")
public class TaPorcCotiz {

    @Id
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Column(name = "porcentaje", nullable = false, precision = 9, scale = 5)
    private BigDecimal porcentaje;

    public TaPorcCotiz() {
    }

    public TaPorcCotiz(Integer anio, BigDecimal porcentaje) {
        this.anio = anio;
        this.porcentaje = porcentaje;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }
}


