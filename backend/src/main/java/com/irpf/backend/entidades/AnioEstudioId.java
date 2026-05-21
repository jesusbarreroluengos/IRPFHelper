package com.irpf.backend.entidades;

import java.io.Serializable;
import java.util.Objects;

/**
 * Clave compuesta (anio, codEstudio) para tablas salariales auxiliares.
 */
public class AnioEstudioId implements Serializable {
    private Integer anio;
    private String codEstudio;

    public AnioEstudioId() {
    }

    public AnioEstudioId(Integer anio, String codEstudio) {
        this.anio = anio;
        this.codEstudio = codEstudio;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnioEstudioId that = (AnioEstudioId) o;
        return Objects.equals(anio, that.anio) && Objects.equals(codEstudio, that.codEstudio);
    }

    @Override
    public int hashCode() {
        return Objects.hash(anio, codEstudio);
    }
}

