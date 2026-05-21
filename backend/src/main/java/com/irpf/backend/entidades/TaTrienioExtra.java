package com.irpf.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de TaTrienioExtra.
 */
@Entity
@Table(name = "ta_trienio_extra")
@IdClass(AnioEstudioId.class)
public class TaTrienioExtra {

    @Id
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Id
    @Column(name = "cod_estudio", length = 1, nullable = false)
    private String codEstudio;

    @Column(name = "importe", nullable = false, precision = 9, scale = 2)
    private BigDecimal importe;

    @ManyToOne
    @JoinColumn(name = "cod_estudio", referencedColumnName = "cod_estudio", insertable = false, updatable = false)
    private TaEstudios estudio;

    public TaTrienioExtra() {
    }

    public TaTrienioExtra(Integer anio, String codEstudio, BigDecimal importe) {
        this.anio = anio;
        this.codEstudio = codEstudio;
        this.importe = importe;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getCodEstudio() {
        return codEstudio;
    }

    public void setCodEstudio(String codEstudio) {
        this.codEstudio = codEstudio;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }

    public TaEstudios getEstudio() {
        return estudio;
    }

    public void setEstudio(TaEstudios estudio) {
        this.estudio = estudio;
    }
}


