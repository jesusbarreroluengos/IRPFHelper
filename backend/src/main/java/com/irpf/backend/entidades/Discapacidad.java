package com.irpf.backend.entidades;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de Discapacidad.
 */
@Entity
@Table(name = "ta_discapacidad")
public class Discapacidad {

    @Id
    @Column(name = "COD_DISCAPACIDAD", length = 1)
    private String codDiscapacidad;

    @Column(name = "DESC_DISCAPACIDAD", length = 50, nullable = false)
    private String descDiscapacidad;

    // === Constructores ===
    public Discapacidad() {
    }

    public Discapacidad(String codDiscapacidad, String descDiscapacidad) {
        this.codDiscapacidad = codDiscapacidad;
        this.descDiscapacidad = descDiscapacidad;
    }

    // === Getters and Setters ===

    public String getCodDiscapacidad() {
        return codDiscapacidad;
    }

    public void setCodDiscapacidad(String codDiscapacidad) {
        this.codDiscapacidad = codDiscapacidad;
    }

    public String getDescDiscapacidad() {
        return descDiscapacidad;
    }

    public void setDescDiscapacidad(String descDiscapacidad) {
        this.descDiscapacidad = descDiscapacidad;
    }

    @Override
    public String toString() {
        return "Discapacidad{" +
                "codDiscapacidad='" + codDiscapacidad + '\'' +
                ", descDiscapacidad='" + descDiscapacidad + '\'' +
                '}';
    }
}

