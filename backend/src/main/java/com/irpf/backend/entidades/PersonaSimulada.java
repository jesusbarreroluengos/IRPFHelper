package com.irpf.backend.entidades;

import jakarta.persistence.*;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de PersonaSimulada.
 */
@Entity
@Table(name = "PERSONA_SIMULADA")
public class PersonaSimulada {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PERSONA")
    private Long idPersona;

    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NOMBRE", length = 40)
    private String nombre;

    @Column(name = "NIF_FICTICIO", length = 9)
    private String nifFicticio;

    @Column(name = "ANIO_NAC", nullable = false)
    private Short anioNac;

    @Column(name = "COD_DISCAPACIDAD", length = 1)
    private String codDiscapacidad;

    @Column(name = "COD_SITFAM", length = 1)
    private String codSitfam;

    @Column(name = "IND_CEUMELILLA", length = 1, nullable = false)
    private String indCeumelilla = "N";

    @Column(name = "COD_CONTRATO", length = 1)
    private String codContrato;

    @Column(name = "IMP_PENSION_CONYUGE", nullable = false, precision = 9, scale = 2)
    private BigDecimal impPensionConyuge = BigDecimal.ZERO;

    @Column(name = "IMP_PENSION_HIJOS", nullable = false, precision = 9, scale = 2)
    private BigDecimal impPensionHijos = BigDecimal.ZERO;

    @Column(name = "ID_COMUNIDAD", nullable = false)
    private Integer idComunidad = 1;

    // === Relaciones JPA ===

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID", referencedColumnName = "ID", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_DISCAPACIDAD", referencedColumnName = "COD_DISCAPACIDAD", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Discapacidad discapacidad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_SITFAM", referencedColumnName = "COD_SITFAM", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private SituacionFamiliar situacionFamiliar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COD_CONTRATO", referencedColumnName = "COD_CONTRATO", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_COMUNIDAD", referencedColumnName = "ID_COMUNIDAD", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private TaComunidad comunidad;

    // === Constructores ===

    public PersonaSimulada() {
    }

    public PersonaSimulada(Long id, String nombre, String nifFicticio, Short anioNac, 
                          String codDiscapacidad, String codSitfam, String indCeumelilla, 
                          String codContrato, BigDecimal impPensionConyuge, BigDecimal impPensionHijos,
                          Integer idComunidad) {
        this.id = id;
        this.nombre = nombre;
        this.nifFicticio = nifFicticio;
        this.anioNac = anioNac;
        this.codDiscapacidad = codDiscapacidad;
        this.codSitfam = codSitfam;
        this.indCeumelilla = indCeumelilla;
        this.codContrato = codContrato;
        this.impPensionConyuge = impPensionConyuge;
        this.impPensionHijos = impPensionHijos;
        this.idComunidad = idComunidad;
    }

    // === Getters and Setters ===

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNifFicticio() {
        return nifFicticio;
    }

    public void setNifFicticio(String nifFicticio) {
        this.nifFicticio = nifFicticio;
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

    public String getCodSitfam() {
        return codSitfam;
    }

    public void setCodSitfam(String codSitfam) {
        this.codSitfam = codSitfam;
    }

    public String getIndCeumelilla() {
        return indCeumelilla;
    }

    public void setIndCeumelilla(String indCeumelilla) {
        this.indCeumelilla = indCeumelilla;
    }

    public String getCodContrato() {
        return codContrato;
    }

    public void setCodContrato(String codContrato) {
        this.codContrato = codContrato;
    }

    // === Getters para las relaciones ===

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Discapacidad getDiscapacidad() {
        return discapacidad;
    }

    public void setDiscapacidad(Discapacidad discapacidad) {
        this.discapacidad = discapacidad;
    }

    public SituacionFamiliar getSituacionFamiliar() {
        return situacionFamiliar;
    }

    public void setSituacionFamiliar(SituacionFamiliar situacionFamiliar) {
        this.situacionFamiliar = situacionFamiliar;
    }

    public Contrato getContrato() {
        return contrato;
    }

    public void setContrato(Contrato contrato) {
        this.contrato = contrato;
    }

    public BigDecimal getImpPensionConyuge() {
        return impPensionConyuge;
    }

    public void setImpPensionConyuge(BigDecimal impPensionConyuge) {
        this.impPensionConyuge = impPensionConyuge;
    }

    public BigDecimal getImpPensionHijos() {
        return impPensionHijos;
    }

    public void setImpPensionHijos(BigDecimal impPensionHijos) {
        this.impPensionHijos = impPensionHijos;
    }

    public Integer getIdComunidad() {
        return idComunidad;
    }

    public void setIdComunidad(Integer idComunidad) {
        this.idComunidad = idComunidad;
    }

    public TaComunidad getComunidad() {
        return comunidad;
    }

    public void setComunidad(TaComunidad comunidad) {
        this.comunidad = comunidad;
    }

    @Override
    public String toString() {
        return "PersonaSimulada{" +
                "idPersona=" + idPersona +
                ", id=" + id +
                ", nombre='" + nombre + '\'' +
                ", nifFicticio='" + nifFicticio + '\'' +
                ", anioNac=" + anioNac +
                ", codDiscapacidad='" + codDiscapacidad + '\'' +
                ", codSitfam='" + codSitfam + '\'' +
                ", indCeumelilla='" + indCeumelilla + '\'' +
                ", codContrato='" + codContrato + '\'' +
                ", impPensionConyuge=" + impPensionConyuge +
                ", impPensionHijos=" + impPensionHijos +
                ", idComunidad=" + idComunidad + '}';
    }
}

