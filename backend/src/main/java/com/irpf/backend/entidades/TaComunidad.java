package com.irpf.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de TaComunidad.
 */
@Entity
@Table(name = "ta_comunidad")
public class TaComunidad {

    @Id
    @Column(name = "ID_COMUNIDAD", nullable = false)
    private Integer idComunidad;

    @Column(name = "DESC_COMUNIDAD", length = 45, nullable = false)
    private String descComunidad;

    public TaComunidad() {
    }

    public TaComunidad(Integer idComunidad, String descComunidad) {
        this.idComunidad = idComunidad;
        this.descComunidad = descComunidad;
    }

    public Integer getIdComunidad() {
        return idComunidad;
    }

    public void setIdComunidad(Integer idComunidad) {
        this.idComunidad = idComunidad;
    }

    public String getDescComunidad() {
        return descComunidad;
    }

    public void setDescComunidad(String descComunidad) {
        this.descComunidad = descComunidad;
    }
}

