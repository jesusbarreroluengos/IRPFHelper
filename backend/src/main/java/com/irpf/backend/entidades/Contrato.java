package com.irpf.backend.entidades;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de Contrato.
 */
@Entity
@Table(name = "ta_contrato")
public class Contrato {

    @Id
    @Column(name = "COD_CONTRATO", length = 1)
    private String codContrato;

    @Column(name = "DESC_CONTRATO", length = 30, nullable = false)
    private String descContrato;

    // === Constructores ===
    
    public Contrato() {
    }

    public Contrato(String codContrato, String descContrato) {
        this.codContrato = codContrato;
        this.descContrato = descContrato;
    }

    // === Getters and Setters ===

    public String getCodContrato() {
        return codContrato;
    }

    public void setCodContrato(String codContrato) {
        this.codContrato = codContrato;
    }

    public String getDescContrato() {
        return descContrato;
    }

    public void setDescContrato(String descContrato) {
        this.descContrato = descContrato;
    }

    @Override
    public String toString() {
        return "Contrato{" +
                "codContrato='" + codContrato + '\'' +
                ", descContrato='" + descContrato + '\'' +
                '}';
    }
}

