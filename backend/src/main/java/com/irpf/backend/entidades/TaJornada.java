package com.irpf.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de TaJornada.
 */
@Entity
@Table(name = "ta_jornada")
public class TaJornada {

    @Id
    @Column(name = "cod_jornada")
    private Integer codJornada;

    @Column(name = "desc_jornada", length = 30, nullable = false)
    private String descJornada;

    @Column(name = "porcentaje", precision = 9, scale = 4, nullable = false)
    private BigDecimal porcentaje;

    public Integer getCodJornada() {
        return codJornada;
    }

    public void setCodJornada(Integer codJornada) {
        this.codJornada = codJornada;
    }

    public String getDescJornada() {
        return descJornada;
    }

    public void setDescJornada(String descJornada) {
        this.descJornada = descJornada;
    }

    public BigDecimal getPorcentaje() {
        return porcentaje;
    }

    public void setPorcentaje(BigDecimal porcentaje) {
        this.porcentaje = porcentaje;
    }
}



