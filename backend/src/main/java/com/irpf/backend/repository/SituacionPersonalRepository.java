package com.irpf.backend.repository;

import com.irpf.backend.entidades.SituacionPersonal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio para el catalogo de situaciones personales.
 */
@Repository
public interface SituacionPersonalRepository extends JpaRepository<SituacionPersonal, String> {
    
    /**
     * Busca una situacion personal por su codigo.
     *
     * @param codSituper codigo de situacion personal
     * @return situacion personal encontrada
     */
    SituacionPersonal findByCodSituper(String codSituper);
    
    /**
     * Busca una situacion personal por coincidencia parcial de descripcion.
     *
     * @param descripcion texto de busqueda
     * @return primera situacion personal coincidente
     */
    SituacionPersonal findByDescSituperContainingIgnoreCase(String descripcion);
}


