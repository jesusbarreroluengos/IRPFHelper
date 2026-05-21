package com.irpf.backend.entidades;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave compuesta utilizada por JPA para identificar de forma unica la entidad TaEspecificoId.
 */
public class TaEspecificoId implements Serializable {
    private Integer anio;
    private String codEstudio;
    private Integer idComunidad;

    public TaEspecificoId() {
    }

    public TaEspecificoId(Integer anio, String codEstudio, Integer idComunidad) {
        this.anio = anio;
        this.codEstudio = codEstudio;
        this.idComunidad = idComunidad;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaEspecificoId that = (TaEspecificoId) o;
        return Objects.equals(anio, that.anio)
                && Objects.equals(codEstudio, that.codEstudio)
                && Objects.equals(idComunidad, that.idComunidad);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anio, codEstudio, idComunidad);
    }
}

