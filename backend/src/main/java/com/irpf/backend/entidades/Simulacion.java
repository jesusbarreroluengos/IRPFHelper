package com.irpf.backend.entidades;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Entidad JPA que representa la tabla y los datos de dominio de Simulacion.
 */
@Entity
@Table(name = "simulacion")
public class Simulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_simulacion")
    private Long idSimulacion;

    @Column(name = "id_persona", nullable = false)
    private Long idPersona;

    @Column(name = "ejercicio", nullable = false)
    private Integer ejercicio;

    @Column(name = "imp_bruto_abonado", nullable = false, precision = 9, scale = 2)
    private BigDecimal impBrutoAbonado = BigDecimal.ZERO;

    @Column(name = "imp_retenciones_practicadas", nullable = false, precision = 9, scale = 2)
    private BigDecimal impRetencionesPracticadas = BigDecimal.ZERO;

    @Column(name = "imp_gastos_realizados", nullable = false, precision = 9, scale = 2)
    private BigDecimal impGastosRealizados = BigDecimal.ZERO;

    @Column(name = "imp_bruto_pendiente", nullable = false, precision = 9, scale = 2)
    private BigDecimal impBrutoPendiente = BigDecimal.ZERO;

    @Column(name = "imp_gastos_pendiente", nullable = false, precision = 9, scale = 2)
    private BigDecimal impGastosPendiente = BigDecimal.ZERO;

    @Column(name = "Fecha_hasta_abonado")
    private LocalDate fechaHastaAbonado;

    @Column(name = "Porc_IRPF", nullable = false, precision = 5, scale = 2)
    private BigDecimal porcIrpf = BigDecimal.ZERO;

    public Long getIdSimulacion() {
        return idSimulacion;
    }

    public void setIdSimulacion(Long idSimulacion) {
        this.idSimulacion = idSimulacion;
    }

    public Long getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(Long idPersona) {
        this.idPersona = idPersona;
    }

    public Integer getEjercicio() {
        return ejercicio;
    }

    public void setEjercicio(Integer ejercicio) {
        this.ejercicio = ejercicio;
    }

    public BigDecimal getImpBrutoAbonado() {
        return impBrutoAbonado;
    }

    public void setImpBrutoAbonado(BigDecimal impBrutoAbonado) {
        this.impBrutoAbonado = impBrutoAbonado;
    }

    public BigDecimal getImpRetencionesPracticadas() {
        return impRetencionesPracticadas;
    }

    public void setImpRetencionesPracticadas(BigDecimal impRetencionesPracticadas) {
        this.impRetencionesPracticadas = impRetencionesPracticadas;
    }

    public BigDecimal getImpGastosRealizados() {
        return impGastosRealizados;
    }

    public void setImpGastosRealizados(BigDecimal impGastosRealizados) {
        this.impGastosRealizados = impGastosRealizados;
    }

    public BigDecimal getImpBrutoPendiente() {
        return impBrutoPendiente;
    }

    public void setImpBrutoPendiente(BigDecimal impBrutoPendiente) {
        this.impBrutoPendiente = impBrutoPendiente;
    }

    public BigDecimal getImpGastosPendiente() {
        return impGastosPendiente;
    }

    public void setImpGastosPendiente(BigDecimal impGastosPendiente) {
        this.impGastosPendiente = impGastosPendiente;
    }

    public LocalDate getFechaHastaAbonado() {
        return fechaHastaAbonado;
    }

    public void setFechaHastaAbonado(LocalDate fechaHastaAbonado) {
        this.fechaHastaAbonado = fechaHastaAbonado;
    }

    public BigDecimal getPorcIrpf() {
        return porcIrpf;
    }

    public void setPorcIrpf(BigDecimal porcIrpf) {
        this.porcIrpf = porcIrpf;
    }
}


