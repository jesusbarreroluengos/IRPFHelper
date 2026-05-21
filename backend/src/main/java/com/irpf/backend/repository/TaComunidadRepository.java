package com.irpf.backend.repository;

import com.irpf.backend.entidades.TaComunidad;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repositorio del catalogo de comunidades autonomas.
 */
public interface TaComunidadRepository extends JpaRepository<TaComunidad, Integer> {
}
