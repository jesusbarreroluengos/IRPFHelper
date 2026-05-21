package com.irpf.backend.repository;

import com.irpf.backend.entidades.AnioEstudioId;
import com.irpf.backend.entidades.TaDestino;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio de complementos de destino por anio y estudio.
 */
@Repository
public interface TaDestinoRepository extends JpaRepository<TaDestino, AnioEstudioId> {

    /**
     * Busca el importe de destino para un anio y codigo de estudio.
     *
     * @param anio ejercicio de referencia
     * @param codEstudio codigo de estudio
     * @return fila encontrada si existe
     */
    Optional<TaDestino> findByAnioAndCodEstudio(Integer anio, String codEstudio);
}

