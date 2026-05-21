package com.irpf.backend.repository;

import com.irpf.backend.entidades.TaJornada;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio del catalogo de tipos de jornada y porcentajes asociados.
 */
public interface TaJornadaRepository extends JpaRepository<TaJornada, Integer> {
}


