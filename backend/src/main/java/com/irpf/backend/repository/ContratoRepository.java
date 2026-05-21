package com.irpf.backend.repository;

import com.irpf.backend.entidades.Contrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, String> {
    
    /**
     * Busca un contrato por su código
     * @param codContrato código del contrato
     * @return Optional con el contrato encontrado
     */
    Optional<Contrato> findByCodContrato(String codContrato);
    
    /**
     * Verifica si existe un contrato con el código especificado
     * @param codContrato código del contrato
     * @return true si existe, false en caso contrario
     */
    boolean existsByCodContrato(String codContrato);
}
