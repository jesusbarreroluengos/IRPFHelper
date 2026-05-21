package com.irpf.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de TaSexenio.
 */
@Entity
@Table(name = "ta_sexenio")
@IdClass(TaSexenioId.class)
public class TaSexenio {

    @Id
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Id
    @Column(name = "num_sexenio", length = 1, nullable = false)
    private String numSexenio;

    @Id
    @Column(name = "id_comunidad", nullable = false)
    private Integer idComunidad;

    @Column(name = "importe", nullable = false, precision = 9, scale = 2)
    private BigDecimal importe;

    public TaSexenio() {
    }

    public TaSexenio(Integer anio, String numSexenio, Integer idComunidad, BigDecimal importe) {
        this.anio = anio;
        this.numSexenio = numSexenio;
        this.idComunidad = idComunidad;
        this.importe = importe;
    }

    public Integer getAnio() {
        return anio;
    }

    public void setAnio(Integer anio) {
        this.anio = anio;
    }

    public String getNumSexenio() {
        return numSexenio;
    }

    public void setNumSexenio(String numSexenio) {
        this.numSexenio = numSexenio;
    }

    public Integer getIdComunidad() {
        return idComunidad;
    }

    public void setIdComunidad(Integer idComunidad) {
        this.idComunidad = idComunidad;
    }

    public BigDecimal getImporte() {
        return importe;
    }

    public void setImporte(BigDecimal importe) {
        this.importe = importe;
    }
}


