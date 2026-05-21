package com.irpf.backend.entidades;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de SituacionFamiliar.
 */
@Entity
@Table(name = "ta_situacion_familiar")
public class SituacionFamiliar {

    @Id
    @Column(name = "COD_SITFAM", length = 1)
    private String codSitfam;

    @Column(name = "DESC_SITFAM", length = 30, nullable = false)
    private String descSitfam;

    // === Constructores ===
    
    public SituacionFamiliar() {
    }

    public SituacionFamiliar(String codSitfam, String descSitfam) {
        this.codSitfam = codSitfam;
        this.descSitfam = descSitfam;
    }

    // === Getters and Setters ===

    public String getCodSitfam() {
        return codSitfam;
    }

    public void setCodSitfam(String codSitfam) {
        this.codSitfam = codSitfam;
    }

    public String getDescSitfam() {
        return descSitfam;
    }

    public void setDescSitfam(String descSitfam) {
        this.descSitfam = descSitfam;
    }

    @Override
    public String toString() {
        return "SituacionFamiliar{" +
                "codSitfam='" + codSitfam + '\'' +
                ", descSitfam='" + descSitfam + '\'' +
                '}';
    }
}

