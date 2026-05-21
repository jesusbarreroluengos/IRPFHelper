package com.irpf.backend.repository;

import com.irpf.backend.entidades.AnioEstudioId;
import com.irpf.backend.entidades.TaSueldoExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de importes de sueldo extraordinario por anio y estudio.
 */
@Repository
public interface TaSueldoExtraRepository extends JpaRepository<TaSueldoExtra, AnioEstudioId> {

    /**
     * Busca sueldo extraordinario por anio y codigo de estudio.
     *
     * @param anio ejercicio
     * @param codEstudio codigo de estudio
     * @return importe encontrado si existe
     */
    Optional<TaSueldoExtra> findByAnioAndCodEstudio(Integer anio, String codEstudio);
}

