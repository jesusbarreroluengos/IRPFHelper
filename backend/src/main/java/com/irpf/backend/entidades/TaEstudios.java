package com.irpf.backend.entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de TaEstudios.
 */
@Entity
@Table(name = "ta_estudios")
public class TaEstudios {

    @Id
    @Column(name = "cod_estudio", length = 1, nullable = false)
    private String codEstudio;

    @Column(name = "desc_estudio", length = 40)
    private String descEstudio;

    public TaEstudios() {
    }

    public TaEstudios(String codEstudio, String descEstudio) {
        this.codEstudio = codEstudio;
        this.descEstudio = descEstudio;
    }

    public String getCodEstudio() {
        return codEstudio;
    }

    public void setCodEstudio(String codEstudio) {
        this.codEstudio = codEstudio;
    }

    public String getDescEstudio() {
        return descEstudio;
    }

    public void setDescEstudio(String descEstudio) {
        this.descEstudio = descEstudio;
    }
}


