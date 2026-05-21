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
 * Entidad JPA que representa la tabla y los datos de dominio de TaEspecifico.
 */
@Entity
@Table(name = "ta_especifico")
@IdClass(TaEspecificoId.class)
public class TaEspecifico {

    @Id
    @Column(name = "anio", nullable = false)
    private Integer anio;

    @Id
    @Column(name = "cod_estudio", length = 1, nullable = false)
    private String codEstudio;

    @Id
    @Column(name = "id_comunidad", nullable = false)
    private Integer idComunidad;

    @Column(name = "importe", nullable = false, precision = 9, scale = 2)
    private BigDecimal importe;

    @ManyToOne
    @JoinColumn(name = "cod_estudio", referencedColumnName = "cod_estudio", insertable = false, updatable = false)
    private TaEstudios estudio;

    @ManyToOne
    @JoinColumn(name = "id_comunidad", referencedColumnName = "ID_COMUNIDAD", insertable = false, updatable = false)
    private TaComunidad comunidad;

    public TaEspecifico() {
    }

    public TaEspecifico(Integer anio, String codEstudio, Integer idComunidad, BigDecimal importe) {
        this.anio = anio;
        this.codEstudio = codEstudio;
        this.idComunidad = idComunidad;
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

    public TaEstudios getEstudio() {
        return estudio;
    }

    public void setEstudio(TaEstudios estudio) {
        this.estudio = estudio;
    }

    public TaComunidad getComunidad() {
        return comunidad;
    }

    public void setComunidad(TaComunidad comunidad) {
        this.comunidad = comunidad;
    }
}


