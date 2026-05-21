package com.irpf.backend.repository;

import com.irpf.backend.entidades.Ascendiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AscendienteRepository extends JpaRepository<Ascendiente, Long> {
    
    /**
     * Busca todos los ascendientes de una persona específica
     * @param idPersona ID de la persona
     * @return Lista de ascendientes de la persona
     */
    List<Ascendiente> findByIdPersona(Long idPersona);
    
    /**
     * Busca ascendientes por código de discapacidad
     * @param codDiscapacidad Código de discapacidad
     * @return Lista de ascendientes con esa discapacidad
     */
    List<Ascendiente> findByCodDiscapacidad(String codDiscapacidad);
    
    /**
     * Busca ascendientes por año de nacimiento
     * @param anioNac Año de nacimiento
     * @return Lista de ascendientes nacidos en ese año
     */
    List<Ascendiente> findByAnioNac(Short anioNac);
    
    /**
     * Busca ascendientes con indicador de compartido específico
     * @param indCompartido Indicador de compartido
     * @return Lista de ascendientes con ese indicador de compartido
     */
    List<Ascendiente> findByIndCompartido(Short indCompartido);
    
    /**
     * Cuenta el número de ascendientes de una persona
     * @param idPersona ID de la persona
     * @return Número de ascendientes
     */
    long countByIdPersona(Long idPersona);
    
    /**
     * Busca ascendientes de una persona con una discapacidad específica
     * @param idPersona ID de la persona
     * @param codDiscapacidad Código de discapacidad
     * @return Lista de ascendientes de la persona con esa discapacidad
     */
    List<Ascendiente> findByIdPersonaAndCodDiscapacidad(Long idPersona, String codDiscapacidad);
    
    /**
     * Busca ascendientes de una persona con indicador de compartido específico
     * @param idPersona ID de la persona
     * @param indCompartido Indicador de compartido
     * @return Lista de ascendientes de la persona con ese indicador de compartido
     */
    List<Ascendiente> findByIdPersonaAndIndCompartido(Long idPersona, Short indCompartido);
    
    /**
     * Consulta personalizada para obtener ascendientes con información completa
     * @param idPersona ID de la persona
     * @return Lista de ascendientes con datos relacionados
     */
    @Query("SELECT a FROM Ascendiente a LEFT JOIN FETCH a.discapacidad WHERE a.idPersona = :idPersona")
    List<Ascendiente> findByIdPersonaWithDiscapacidad(@Param("idPersona") Long idPersona);
}


