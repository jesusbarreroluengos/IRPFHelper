package com.irpf.backend.entidades;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa un ascendiente asociado a una persona simulada.
 */
@Entity
@Table(name = "ASCENDIENTES")
public class Ascendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ASCENDIENTE")
    private Long idAscendiente;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "ANIO_NAC", nullable = false)
    private Short anioNac;

    @Column(name = "COD_DISCAPACIDAD", length = 1, nullable = false)
    private String codDiscapacidad;

    @Column(name = "IND_COMPARTIDO",  nullable = false)
    private Short indCompartido;

    // === Relaciones JPA ===

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PERSONA", referencedColumnName = "ID_PERSONA", insertable = false, updatable = false)
    private PersonaSimulada personaSimulada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_DISCAPACIDAD", referencedColumnName = "COD_DISCAPACIDAD", insertable = false, updatable = false)
    private Discapacidad discapacidad;

    // === Constructores ===

    public Ascendiente() {
    }

    public Ascendiente(Long idPersona, Short anioNac, String codDiscapacidad, Short indCompartido) {
        this.idPersona = idPersona;
        this.anioNac = anioNac;
        this.codDiscapacidad = codDiscapacidad;
        this.indCompartido = indCompartido;
    }

    // === Getters and Setters ===

    public Long getIdAscendiente() {
        return idAscendiente;
    }

    public void setIdAscendiente(Long idAscendiente) {
        this.idAscendiente = idAscendiente;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public Short getAnioNac() {
        return anioNac;
    }

    public void setAnioNac(Short anioNac) {
        this.anioNac = anioNac;
    }

    public String getCodDiscapacidad() {
        return codDiscapacidad;
    }

    public void setCodDiscapacidad(String codDiscapacidad) {
        this.codDiscapacidad = codDiscapacidad;
    }

    public Short getIndCompartido() {
        return indCompartido;
    }

    public void setIndCompartido(Short indCompartido) {
        this.indCompartido = indCompartido;
    }

    // === Getters para las relaciones ===

    public PersonaSimulada getPersonaSimulada() {
        return personaSimulada;
    }

    public void setPersonaSimulada(PersonaSimulada personaSimulada) {
        this.personaSimulada = personaSimulada;
    }

    public Discapacidad getDiscapacidad() {
        return discapacidad;
    }

    public void setDiscapacidad(Discapacidad discapacidad) {
        this.discapacidad = discapacidad;
    }

    @Override
    public String toString() {
        return "Ascendiente{" +
                "idAscendiente=" + idAscendiente +
                ", idPersona=" + idPersona +
                ", anioNac=" + anioNac +
                ", codDiscapacidad='" + codDiscapacidad + '\'' +
                ", indCompartido='" + indCompartido + '\'' +
                '}';
    }
}


