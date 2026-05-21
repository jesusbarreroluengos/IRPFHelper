package com.irpf.backend.repository;

import com.irpf.backend.entidades.Simulacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio base para persistencia de simulaciones de retribuciones.
 */
@Repository
public interface SimulacionRepository extends JpaRepository<Simulacion, Long> {
}

