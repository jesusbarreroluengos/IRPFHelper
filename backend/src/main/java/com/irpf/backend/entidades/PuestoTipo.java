package com.irpf.backend.entidades;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de PuestoTipo.
 */
@Entity
@Table(name = "puesto_tipo", uniqueConstraints = {
        @UniqueConstraint(name = "uk_puesto_persona_nombre", columnNames = {"id_persona", "nom_puesto"})
})
public class PuestoTipo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_puesto_tipo")
    private Long idPuestoTipo;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(name = "nom_puesto", length = 45, nullable = false)
    private String nomPuesto;

    @Column(name = "cod_estudio", length = 1)
    private String codEstudio;

    @Column(name = "num_trienios_a1", nullable = false)
    private Integer numTrieniosA1 = 0;

    @Column(name = "num_trienios_a2", nullable = false)
    private Integer numTrieniosA2 = 0;

    @Column(name = "num_sexenios", nullable = false)
    private Integer numSexenios = 0;

    @Column(name = "cod_jornada", nullable = false)
    private Integer codJornada = 1;

    @Column(name = "importe_espec_docente", nullable = false, precision = 9, scale = 2)
    private BigDecimal importeEspecDocente = BigDecimal.ZERO;

    @Column(name = "importe_otros_abonos_mes", precision = 9, scale = 2)
    private BigDecimal importeOtrosAbonosMes = BigDecimal.ZERO;

    @Column(name = "id_comunidad", nullable = false)
    private Integer idComunidad;

    public Integer getCodJornada() {
        return codJornada;
    }

    public void setCodJornada(Integer codJornada) {
        this.codJornada = codJornada;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_persona", referencedColumnName = "ID_PERSONA", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PersonaSimulada personaSimulada;

    public PuestoTipo() {
    }

    public Long getIdPuestoTipo() {
        return idPuestoTipo;
    }

    public void setIdPuestoTipo(Long idPuestoTipo) {
        this.idPuestoTipo = idPuestoTipo;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public String getNomPuesto() {
        return nomPuesto;
    }

    public void setNomPuesto(String nomPuesto) {
        this.nomPuesto = nomPuesto;
    }

    public String getCodEstudio() {
        return codEstudio;
    }

    public void setCodEstudio(String codEstudio) {
        this.codEstudio = codEstudio;
    }

    public Integer getNumTrieniosA1() {
        return numTrieniosA1;
    }

    public void setNumTrieniosA1(Integer numTrieniosA1) {
        this.numTrieniosA1 = numTrieniosA1;
    }

    public Integer getNumTrieniosA2() {
        return numTrieniosA2;
    }

    public void setNumTrieniosA2(Integer numTrieniosA2) {
        this.numTrieniosA2 = numTrieniosA2;
    }

    public Integer getNumSexenios() {
        return numSexenios;
    }

    public void setNumSexenios(Integer numSexenios) {
        this.numSexenios = numSexenios;
    }

    public BigDecimal getImporteEspecDocente() {
        return importeEspecDocente;
    }

    public void setImporteEspecDocente(BigDecimal importeEspecDocente) {
        this.importeEspecDocente = importeEspecDocente;
    }

    public BigDecimal getImporteOtrosAbonosMes() {
        return importeOtrosAbonosMes;
    }

    public void setImporteOtrosAbonosMes(BigDecimal importeOtrosAbonosMes) {
        this.importeOtrosAbonosMes = importeOtrosAbonosMes;
    }

    public Integer getIdComunidad() {
        return idComunidad;
    }

    public void setIdComunidad(Integer idComunidad) {
        this.idComunidad = idComunidad;
    }

    public PersonaSimulada getPersonaSimulada() {
        return personaSimulada;
    }

    public void setPersonaSimulada(PersonaSimulada personaSimulada) {
        this.personaSimulada = personaSimulada;
    }
}


