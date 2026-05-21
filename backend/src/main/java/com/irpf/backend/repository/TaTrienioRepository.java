package com.irpf.backend.repository;

import com.irpf.backend.entidades.AnioEstudioId;
import com.irpf.backend.entidades.TaTrienio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de importes de trienios por anio y estudio.
 */
@Repository
public interface TaTrienioRepository extends JpaRepository<TaTrienio, AnioEstudioId> {

    /**
     * Busca trienio por anio y codigo de estudio.
     *
     * @param anio ejercicio
     * @param codEstudio codigo de estudio
     * @return importe encontrado si existe
     */
    Optional<TaTrienio> findByAnioAndCodEstudio(Integer anio, String codEstudio);
}

