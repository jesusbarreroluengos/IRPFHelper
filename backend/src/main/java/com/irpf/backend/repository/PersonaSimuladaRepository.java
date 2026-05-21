package com.irpf.backend.repository;

import com.irpf.backend.entidades.PersonaSimulada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PersonaSimuladaRepository extends JpaRepository<PersonaSimulada, Long> {
    
    /**
     * Busca todas las personas simuladas por ID de usuario
     */
    @Query("SELECT ps FROM PersonaSimulada ps WHERE ps.id = :idUsuario")
    List<PersonaSimulada> findByIdUsuario(@Param("idUsuario") Long idUsuario);
    
    /**
     * Busca una persona simulada por NIF ficticio
     */
    Optional<PersonaSimulada> findByNifFicticio(String nifFicticio);
    
    /**
     * Busca todas las personas simuladas por código de discapacidad
     */
    List<PersonaSimulada> findByCodDiscapacidad(String codDiscapacidad);
    
    /**
     * Busca todas las personas simuladas por código de situación familiar
     */
    List<PersonaSimulada> findByCodSitfam(String codSitfam);
    
    /**
     * Busca todas las personas simuladas por código de contrato
     */
    List<PersonaSimulada> findByCodContrato(String codContrato);
    
    /**
     * Busca todas las personas simuladas por indicador de Ceuta/Melilla
     */
    List<PersonaSimulada> findByIndCeumelilla(String indCeumelilla);
    
    /**
     * Busca todas las personas simuladas de un usuario específico con sus relaciones cargadas
     */
    @Query("SELECT ps FROM PersonaSimulada ps " +
           "LEFT JOIN FETCH ps.discapacidad " +
           "LEFT JOIN FETCH ps.situacionFamiliar " +
           "LEFT JOIN FETCH ps.comunidad " +
           "LEFT JOIN FETCH ps.contrato " +
           "WHERE ps.id = :idUsuario")
    List<PersonaSimulada> findByIdUsuarioWithRelations(@Param("idUsuario") Long idUsuario);
    
    /**
     * Verifica si existe una persona simulada con el NIF ficticio dado
     */
    boolean existsByNifFicticio(String nifFicticio);
    
    /**
     * Cuenta el número de personas simuladas por usuario
     */
    @Query("SELECT COUNT(ps) FROM PersonaSimulada ps WHERE ps.id = :idUsuario")
    long countByIdUsuario(@Param("idUsuario") Long idUsuario);
}
