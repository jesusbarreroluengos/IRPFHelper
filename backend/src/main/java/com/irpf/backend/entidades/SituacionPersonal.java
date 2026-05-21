package com.irpf.backend.entidades;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de SituacionPersonal.
 */
@Entity
@Table(name = "ta_situacion_personal")
public class SituacionPersonal {

    @Id
    @Column(name = "COD_SITUPER", length = 1)
    private String codSituper;

    @Column(name = "DESC_SITUPER", length = 30, nullable = false)
    private String descSituper;

    // Constructor por defecto
    public SituacionPersonal() {
    }

    // Constructor con parÃ¡metros
    public SituacionPersonal(String codSituper, String descSituper) {
        this.codSituper = codSituper;
        this.descSituper = descSituper;
    }

    // === Getters and Setters ===

    public String getCodSituper() {
        return codSituper;
    }

    public void setCodSituper(String codSituper) {
        this.codSituper = codSituper;
    }

    public String getDescSituper() {
        return descSituper;
    }

    public void setDescSituper(String descSituper) {
        this.descSituper = descSituper;
    }

    @Override
    public String toString() {
        return "SituacionPersonal{" +
                "codSituper='" + codSituper + '\'' +
                ", descSituper='" + descSituper + '\'' +
                '}';
    }
}



