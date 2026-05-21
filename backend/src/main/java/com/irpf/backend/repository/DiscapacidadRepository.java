package com.irpf.backend.repository;

import com.irpf.backend.entidades.Discapacidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DiscapacidadRepository extends JpaRepository<Discapacidad, String> {
    
    /**
     * Buscar discapacidad por código
     * @param codDiscapacidad código de la discapacidad
     * @return Optional con la discapacidad encontrada
     */
    Optional<Discapacidad> findByCodDiscapacidad(String codDiscapacidad);
    
    /**
     * Verificar si existe una discapacidad con el código dado
     * @param codDiscapacidad código de la discapacidad
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodDiscapacidad(String codDiscapacidad);
}
