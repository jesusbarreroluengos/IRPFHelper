package com.irpf.backend.entidades;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.LocalDate;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de ContratoPersona.
 */
@Entity
@Table(name = "contrato_persona")
public class ContratoPersona {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato_persona")
    private Long idContratoPersona;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "fecha_desde", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDesde;

    @Column(name = "fecha_hasta")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaHasta;

    @Column(name = "id_puesto_tipo", nullable = false)
    private Long idPuestoTipo;

    @Column(name = "ind_vac_no_disfrutadas", length = 1, nullable = false)
    private String indVacNoDisfrutadas = "N";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PERSONA", referencedColumnName = "ID_PERSONA", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PersonaSimulada personaSimulada;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_puesto_tipo", referencedColumnName = "id_puesto_tipo", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PuestoTipo puestoTipo;

    public Long getIdContratoPersona() {
        return idContratoPersona;
    }

    public void setIdContratoPersona(Long idContratoPersona) {
        this.idContratoPersona = idContratoPersona;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public LocalDate getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDate fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDate getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDate fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public Long getIdPuestoTipo() {
        return idPuestoTipo;
    }

    public void setIdPuestoTipo(Long idPuestoTipo) {
        this.idPuestoTipo = idPuestoTipo;
    }

    public String getIndVacNoDisfrutadas() {
        return indVacNoDisfrutadas;
    }

    public void setIndVacNoDisfrutadas(String indVacNoDisfrutadas) {
        this.indVacNoDisfrutadas = indVacNoDisfrutadas;
    }

    public PersonaSimulada getPersonaSimulada() {
        return personaSimulada;
    }

    public void setPersonaSimulada(PersonaSimulada personaSimulada) {
        this.personaSimulada = personaSimulada;
    }

    public PuestoTipo getPuestoTipo() {
        return puestoTipo;
    }

    public void setPuestoTipo(PuestoTipo puestoTipo) {
        this.puestoTipo = puestoTipo;
    }
}


