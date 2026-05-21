package com.irpf.backend.repository;

import com.irpf.backend.entidades.TaEstudios;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repositorio del catalogo de estudios docentes.
 */
@Repository
public interface TaEstudiosRepository extends JpaRepository<TaEstudios, String> {
}

