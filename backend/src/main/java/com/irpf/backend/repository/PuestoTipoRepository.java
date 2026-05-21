package com.irpf.backend.repository;

import com.irpf.backend.entidades.PuestoTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de puestos tipo configurados por persona simulada.
 */
@Repository
public interface PuestoTipoRepository extends JpaRepository<PuestoTipo, Long> {

    /**
     * Obtiene puestos de una persona ordenados por nombre.
     *
     * @param idPersona identificador de persona simulada
     * @return lista de puestos tipo
     */
    List<PuestoTipo> findByIdPersonaOrderByNomPuestoAsc(Long idPersona);

    /**
     * Verifica si existe un puesto con nombre para una persona.
     *
     * @param idPersona identificador de persona
     * @param nomPuesto nombre del puesto
     * @return true si existe coincidencia
     */
    boolean existsByIdPersonaAndNomPuestoIgnoreCase(Long idPersona, String nomPuesto);

    /**
     * Busca un puesto de persona por nombre sin distinguir mayusculas.
     *
     * @param idPersona identificador de persona
     * @param nomPuesto nombre del puesto
     * @return puesto encontrado si existe
     */
    Optional<PuestoTipo> findByIdPersonaAndNomPuestoIgnoreCase(Long idPersona, String nomPuesto);
}

