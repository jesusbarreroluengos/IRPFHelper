package com.irpf.backend.repository;

import com.irpf.backend.entidades.TaPorcCotiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de porcentajes de cotizacion por anio.
 */
@Repository
public interface TaPorcCotizRepository extends JpaRepository<TaPorcCotiz, Integer> {

    /**
     * Busca el porcentaje de cotizacion aplicable a un anio.
     *
     * @param anio ejercicio
     * @return porcentaje de cotizacion si existe
     */
    Optional<TaPorcCotiz> findByAnio(Integer anio);
}

