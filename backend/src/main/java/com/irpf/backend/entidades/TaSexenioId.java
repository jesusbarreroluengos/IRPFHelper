package com.irpf.backend.entidades;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave compuesta utilizada por JPA para identificar de forma unica la entidad TaSexenioId.
 */
public class TaSexenioId implements Serializable {
    private Integer anio;
    private String numSexenio;
    private Integer idComunidad;

    public TaSexenioId() {
    }

    public TaSexenioId(Integer anio, String numSexenio, Integer idComunidad) {
        this.anio = anio;
        this.numSexenio = numSexenio;
        this.idComunidad = idComunidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaSexenioId that = (TaSexenioId) o;
        return Objects.equals(anio, that.anio)
                && Objects.equals(numSexenio, that.numSexenio)
                && Objects.equals(idComunidad, that.idComunidad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anio, numSexenio, idComunidad);
    }
}


