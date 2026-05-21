package com.irpf.backend.repository;

import com.irpf.backend.entidades.AnioEstudioId;
import com.irpf.backend.entidades.TaTrienioExtra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de importes extraordinarios de trienios por anio y estudio.
 */
@Repository
public interface TaTrienioExtraRepository extends JpaRepository<TaTrienioExtra, AnioEstudioId> {

    /**
     * Busca trienio extra por anio y codigo de estudio.
     *
     * @param anio ejercicio
     * @param codEstudio codigo de estudio
     * @return importe encontrado si existe
     */
    Optional<TaTrienioExtra> findByAnioAndCodEstudio(Integer anio, String codEstudio);
}

