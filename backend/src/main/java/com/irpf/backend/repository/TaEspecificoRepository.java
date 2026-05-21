package com.irpf.backend.repository;

import com.irpf.backend.entidades.TaEspecifico;
import com.irpf.backend.entidades.TaEspecificoId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de complementos especificos por anio, estudio y comunidad.
 */
@Repository
public interface TaEspecificoRepository extends JpaRepository<TaEspecifico, TaEspecificoId> {

    /**
     * Busca un complemento especifico por su clave compuesta.
     *
     * @param anio ejercicio de referencia
     * @param codEstudio codigo de estudio
     * @param idComunidad identificador de comunidad
     * @return fila encontrada si existe
     */
    Optional<TaEspecifico> findByAnioAndCodEstudioAndIdComunidad(Integer anio, String codEstudio, Integer idComunidad);
}

