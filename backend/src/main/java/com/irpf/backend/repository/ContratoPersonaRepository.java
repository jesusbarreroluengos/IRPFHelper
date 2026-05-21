package com.irpf.backend.repository;

import com.irpf.backend.entidades.ContratoPersona;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para la gestion de contratos asociados a personas simuladas.
 */
@Repository
public interface ContratoPersonaRepository extends JpaRepository<ContratoPersona, Long> {

    /**
     * Recupera los contratos de una persona ordenados por fecha de inicio ascendente.
     *
     * @param idPersona identificador de persona simulada
     * @return lista de contratos ordenada por fecha desde
     */
    List<ContratoPersona> findByIdPersonaOrderByFechaDesdeAsc(Long idPersona);
}

