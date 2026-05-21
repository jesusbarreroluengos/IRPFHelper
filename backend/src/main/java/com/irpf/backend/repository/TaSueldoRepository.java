package com.irpf.backend.repository;

import com.irpf.backend.entidades.AnioEstudioId;
import com.irpf.backend.entidades.TaSueldo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de sueldos base por anio y estudio.
 */
@Repository
public interface TaSueldoRepository extends JpaRepository<TaSueldo, AnioEstudioId> {

    /**
     * Busca sueldo base por anio y codigo de estudio.
     *
     * @param anio ejercicio
     * @param codEstudio codigo de estudio
     * @return importe encontrado si existe
     */
    Optional<TaSueldo> findByAnioAndCodEstudio(Integer anio, String codEstudio);
}

