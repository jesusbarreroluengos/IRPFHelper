package com.irpf.backend.repository;

import com.irpf.backend.entidades.TaSexenio;
import com.irpf.backend.entidades.TaSexenioId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio de importes de sexenios por anio y comunidad.
 */
@Repository
public interface TaSexenioRepository extends JpaRepository<TaSexenio, TaSexenioId> {

    /**
     * Recupera sexenios hasta un numero maximo para un anio y comunidad,
     * ordenados por numero de sexenio ascendente.
     *
     * @param anio ejercicio
     * @param idComunidad identificador de comunidad
     * @param numSexenio numero maximo de sexenio
     * @return lista de sexenios acumulables
     */
    List<TaSexenio> findByAnioAndIdComunidadAndNumSexenioLessThanEqualOrderByNumSexenioAsc(
            Integer anio,
            Integer idComunidad,
            String numSexenio
    );
}

