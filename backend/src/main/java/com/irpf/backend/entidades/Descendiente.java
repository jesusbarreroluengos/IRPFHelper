package com.irpf.backend.entidades;

import jakarta.persistence.*;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de Descendiente.
 */
@Entity
@Table(name = "descendientes")
public class Descendiente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DESCENDIENTE")
    private Long idDescendiente;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "ANIO_NAC", nullable = false)
    private Short anioNac;

    @Column(name = "ANIO_ADOPCION")
    private Short anioAdopcion;

    @Column(name = "IND_PORENTERO", length = 1, nullable = false)
    private String indPorentero;

    @Column(name = "COD_DISCAPACIDAD", length = 1, nullable = false)
    private String codDiscapacidad;

    @Column(name = "IND_MOVRED", length = 1, nullable = false)
    private String indMovred;

    // === Relaciones JPA ===

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PERSONA", referencedColumnName = "ID_PERSONA", insertable = false, updatable = false)
    private PersonaSimulada personaSimulada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_DISCAPACIDAD", referencedColumnName = "COD_DISCAPACIDAD", insertable = false, updatable = false)
    private Discapacidad discapacidad;

    // === Constructores ===

    public Descendiente() {
    }

    public Descendiente(Long idPersona, Short anioNac, Short anioAdopcion, 
                       String indPorentero, String codDiscapacidad, String indMovred) {
        this.idPersona = idPersona;
        this.anioNac = anioNac;
        this.anioAdopcion = anioAdopcion;
        this.indPorentero = indPorentero;
        this.codDiscapacidad = codDiscapacidad;
        this.indMovred = indMovred;
    }

    // === Getters and Setters ===

    public Long getIdDescendiente() {
        return idDescendiente;
    }

    public void setIdDescendiente(Long idDescendiente) {
        this.idDescendiente = idDescendiente;
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

    public Short getAnioAdopcion() {
        return anioAdopcion;
    }

    public void setAnioAdopcion(Short anioAdopcion) {
        this.anioAdopcion = anioAdopcion;
    }

    public String getIndPorentero() {
        return indPorentero;
    }

    public void setIndPorentero(String indPorentero) {
        this.indPorentero = indPorentero;
    }

    public String getCodDiscapacidad() {
        return codDiscapacidad;
    }

    public void setCodDiscapacidad(String codDiscapacidad) {
        this.codDiscapacidad = codDiscapacidad;
    }

    public String getIndMovred() {
        return indMovred;
    }

    public void setIndMovred(String indMovred) {
        this.indMovred = indMovred;
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
        return "Descendiente{" +
                "idDescendiente=" + idDescendiente +
                ", idPersona=" + idPersona +
                ", anioNac=" + anioNac +
                ", anioAdopcion=" + anioAdopcion +
                ", indPorentero='" + indPorentero + '\'' +
                ", codDiscapacidad='" + codDiscapacidad + '\'' +
                ", indMovred='" + indMovred + '\'' +
                '}';
    }
}



