package com.irpf.backend.entidades;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de CalculoIrpf.
 */
@Entity
@Table(name = "CALCULO_IRPF")
public class CalculoIrpf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CALCULO")
    private Long idCalculo;

    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "ID_PERSONA", nullable = false)
    private Long idPersona;

    @Column(name = "EJERCICIO_FISCAL", nullable = false)
    private Short ejercicioFiscal;

    @Column(name = "IMP_PENSION_CONYUGE", nullable = false, precision = 9, scale = 2)
    private BigDecimal impPensionConyuge = BigDecimal.ZERO;

    @Column(name = "IMP_PENSION_HIJOS", nullable = false, precision = 9, scale = 2)
    private BigDecimal impPensionHijos = BigDecimal.ZERO;

    @Column(name = "PORCENTAJE_IRPF", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcentajeIrpf = BigDecimal.ZERO;

    @Column(name = "IMP_COBRADO_BRUTO", nullable = false, precision = 9, scale = 2)
    private BigDecimal impCobradoBruto = BigDecimal.ZERO;

    @Column(name = "IMP_RETENIDO_IRPF", nullable = false, precision = 9, scale = 2)
    private BigDecimal impRetenidoIrpf = BigDecimal.ZERO;

    @Column(name = "IMP_RETRINIDO_GASTOS", nullable = false, precision = 9, scale = 2)
    private BigDecimal impRetrinidoGastos = BigDecimal.ZERO;

    @Column(name = "IMP_PENDIENTE_BRUTO", nullable = false, precision = 9, scale = 2)
    private BigDecimal impPendienteBruto = BigDecimal.ZERO;

    @Column(name = "IMP_PENDIENTE_GASTOS", nullable = false, precision = 9, scale = 2)
    private BigDecimal impPendienteGastos = BigDecimal.ZERO;

    @Column(name = "IMP_PENDIENTE_RETENCIONES", nullable = false, precision = 9, scale = 2)
    private BigDecimal impPendienteRetenciones = BigDecimal.ZERO;

    // === Relaciones JPA ===

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID", referencedColumnName = "ID", insertable = false, updatable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PERSONA", referencedColumnName = "ID_PERSONA", insertable = false, updatable = false)
    private PersonaSimulada personaSimulada;

    // === Constructores ===

    public CalculoIrpf() {
    }

    public CalculoIrpf(Long id, Long idPersona, Short ejercicioFiscal, 
                      BigDecimal impPensionConyuge, BigDecimal impPensionHijos, 
                      BigDecimal porcentajeIrpf, BigDecimal impCobradoBruto, 
                      BigDecimal impRetenidoIrpf, BigDecimal impRetrinidoGastos, 
                      BigDecimal impPendienteBruto, BigDecimal impPendienteGastos, 
                      BigDecimal impPendienteRetenciones) {
        this.id = id;
        this.idPersona = idPersona;
        this.ejercicioFiscal = ejercicioFiscal;
        this.impPensionConyuge = impPensionConyuge;
        this.impPensionHijos = impPensionHijos;
        this.porcentajeIrpf = porcentajeIrpf;
        this.impCobradoBruto = impCobradoBruto;
        this.impRetenidoIrpf = impRetenidoIrpf;
        this.impRetrinidoGastos = impRetrinidoGastos;
        this.impPendienteBruto = impPendienteBruto;
        this.impPendienteGastos = impPendienteGastos;
        this.impPendienteRetenciones = impPendienteRetenciones;
    }

    // === Getters and Setters ===

    public Long getIdCalculo() {
        return idCalculo;
    }

    public void setIdCalculo(Long idCalculo) {
        this.idCalculo = idCalculo;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public Short getEjercicioFiscal() {
        return ejercicioFiscal;
    }

    public void setEjercicioFiscal(Short ejercicioFiscal) {
        this.ejercicioFiscal = ejercicioFiscal;
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

    public BigDecimal getPorcentajeIrpf() {
        return porcentajeIrpf;
    }

    public void setPorcentajeIrpf(BigDecimal porcentajeIrpf) {
        this.porcentajeIrpf = porcentajeIrpf;
    }

    public BigDecimal getImpCobradoBruto() {
        return impCobradoBruto;
    }

    public void setImpCobradoBruto(BigDecimal impCobradoBruto) {
        this.impCobradoBruto = impCobradoBruto;
    }

    public BigDecimal getImpRetenidoIrpf() {
        return impRetenidoIrpf;
    }

    public void setImpRetenidoIrpf(BigDecimal impRetenidoIrpf) {
        this.impRetenidoIrpf = impRetenidoIrpf;
    }

    public BigDecimal getImpRetrinidoGastos() {
        return impRetrinidoGastos;
    }

    public void setImpRetrinidoGastos(BigDecimal impRetrinidoGastos) {
        this.impRetrinidoGastos = impRetrinidoGastos;
    }

    public BigDecimal getImpPendienteBruto() {
        return impPendienteBruto;
    }

    public void setImpPendienteBruto(BigDecimal impPendienteBruto) {
        this.impPendienteBruto = impPendienteBruto;
    }

    public BigDecimal getImpPendienteGastos() {
        return impPendienteGastos;
    }

    public void setImpPendienteGastos(BigDecimal impPendienteGastos) {
        this.impPendienteGastos = impPendienteGastos;
    }

    public BigDecimal getImpPendienteRetenciones() {
        return impPendienteRetenciones;
    }

    public void setImpPendienteRetenciones(BigDecimal impPendienteRetenciones) {
        this.impPendienteRetenciones = impPendienteRetenciones;
    }

    // === Getters para las relaciones ===

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public PersonaSimulada getPersonaSimulada() {
        return personaSimulada;
    }

    public void setPersonaSimulada(PersonaSimulada personaSimulada) {
        this.personaSimulada = personaSimulada;
    }

    @Override
    public String toString() {
        return "CalculoIrpf{" +
                "idCalculo=" + idCalculo +
                ", id=" + id +
                ", idPersona=" + idPersona +
                ", ejercicioFiscal=" + ejercicioFiscal +
                ", impPensionConyuge=" + impPensionConyuge +
                ", impPensionHijos=" + impPensionHijos +
                ", porcentajeIrpf=" + porcentajeIrpf +
                ", impCobradoBruto=" + impCobradoBruto +
                ", impRetenidoIrpf=" + impRetenidoIrpf +
                ", impRetrinidoGastos=" + impRetrinidoGastos +
                ", impPendienteBruto=" + impPendienteBruto +
                ", impPendienteGastos=" + impPendienteGastos +
                ", impPendienteRetenciones=" + impPendienteRetenciones +
                '}';
    }
}

















