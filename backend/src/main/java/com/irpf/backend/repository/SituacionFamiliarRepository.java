package com.irpf.backend.repository;

import com.irpf.backend.entidades.SituacionFamiliar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repositorio para el catalogo de situaciones familiares.
 */
@Repository
public interface SituacionFamiliarRepository extends JpaRepository<SituacionFamiliar, String> {
    
    /**
     * Busca una situación familiar por su código
     * @param codSitfam código de la situación familiar
     * @return Optional con la situación familiar encontrada
     */
    Optional<SituacionFamiliar> findByCodSitfam(String codSitfam);
    
    /**
     * Verifica si existe una situación familiar con el código especificado
     * @param codSitfam código de la situación familiar
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodSitfam(String codSitfam);
}
