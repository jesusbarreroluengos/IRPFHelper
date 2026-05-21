package com.irpf.backend.repository;

import com.irpf.backend.entidades.CalculoIrpf;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio de acceso a datos para calculos de IRPF por usuario, persona y ejercicio.
 */
@Repository
public interface CalculoIrpfRepository extends JpaRepository<CalculoIrpf, Long> {
    
    /**
     * Recupera calculos de IRPF de un usuario.
     *
     * @param id identificador de usuario
     * @return lista de calculos asociados
     */
    List<CalculoIrpf> findByUsuarioId(Long id);
    
    /**
     * Recupera calculos de IRPF de una persona simulada.
     *
     * @param idPersona identificador de persona
     * @return lista de calculos asociados
     */
    List<CalculoIrpf> findByIdPersona(Long idPersona);
    
    /**
     * Recupera calculos de IRPF de un ejercicio fiscal.
     *
     * @param ejercicioFiscal ejercicio fiscal
     * @return lista de calculos del ejercicio
     */
    List<CalculoIrpf> findByEjercicioFiscal(Short ejercicioFiscal);
    
    /**
     * Recupera calculos filtrados por usuario y ejercicio.
     *
     * @param id identificador de usuario
     * @param ejercicioFiscal ejercicio fiscal
     * @return lista de calculos que cumplen el filtro
     */
    List<CalculoIrpf> findByUsuarioIdAndEjercicioFiscal(Long id, Short ejercicioFiscal);
    
    /**
     * Recupera calculos filtrados por persona y ejercicio.
     *
     * @param idPersona identificador de persona
     * @param ejercicioFiscal ejercicio fiscal
     * @return lista de calculos que cumplen el filtro
     */
    List<CalculoIrpf> findByIdPersonaAndEjercicioFiscal(Long idPersona, Short ejercicioFiscal);
    
    /**
     * Obtiene el ultimo calculo registrado para una persona en un ejercicio.
     *
     * @param idPersona identificador de persona
     * @param ejercicioFiscal ejercicio fiscal
     * @return calculo mas reciente si existe
     */
    @Query("SELECT c FROM CalculoIrpf c WHERE c.idPersona = :idPersona AND c.ejercicioFiscal = :ejercicioFiscal ORDER BY c.idCalculo DESC")
    Optional<CalculoIrpf> findUltimoCalculoByPersonaAndEjercicio(@Param("idPersona") Long idPersona, @Param("ejercicioFiscal") Short ejercicioFiscal);
    
    /**
     * Cuenta calculos registrados para un usuario.
     *
     * @param id identificador de usuario
     * @return total de calculos
     */
    long countByUsuarioId(Long id);
    
    /**
     * Cuenta calculos registrados para una persona simulada.
     *
     * @param idPersona identificador de persona
     * @return total de calculos
     */
    long countByIdPersona(Long idPersona);
    
    /**
     * Cuenta calculos registrados en un ejercicio fiscal.
     *
     * @param ejercicioFiscal ejercicio fiscal
     * @return total de calculos
     */
    long countByEjercicioFiscal(Short ejercicioFiscal);
}
