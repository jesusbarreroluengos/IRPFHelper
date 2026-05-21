package com.irpf.backend.repository;

import com.irpf.backend.entidades.Descendiente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DescendienteRepository extends JpaRepository<Descendiente, Long> {
    
    /**
     * Busca todos los descendientes por ID de persona
     */
    @Query("SELECT d FROM Descendiente d WHERE d.idPersona = :idPersona")
    List<Descendiente> findByIdPersona(@Param("idPersona") Long idPersona);
    
    /**
     * Busca todos los descendientes por año de nacimiento
     */
    List<Descendiente> findByAnioNac(Short anioNac);
    
    /**
     * Busca todos los descendientes por año de adopción
     */
    List<Descendiente> findByAnioAdopcion(Short anioAdopcion);
    
    /**
     * Busca todos los descendientes por indicador de por entero
     */
    List<Descendiente> findByIndPorentero(String indPorentero);
    
    /**
     * Busca todos los descendientes por código de discapacidad
     */
    List<Descendiente> findByCodDiscapacidad(String codDiscapacidad);
    
    /**
     * Busca todos los descendientes por indicador de movilidad reducida
     */
    List<Descendiente> findByIndMovred(String indMovred);
    
    /**
     * Busca todos los descendientes de una persona específica con sus relaciones cargadas
     */
    @Query("SELECT d FROM Descendiente d " +
           "LEFT JOIN FETCH d.personaSimulada " +
           "LEFT JOIN FETCH d.discapacidad " +
           "WHERE d.idPersona = :idPersona")
    List<Descendiente> findByIdPersonaWithRelations(@Param("idPersona") Long idPersona);
    
    /**
     * Busca descendientes por rango de años de nacimiento
     */
    @Query("SELECT d FROM Descendiente d WHERE d.anioNac BETWEEN :anioInicio AND :anioFin")
    List<Descendiente> findByAnioNacBetween(@Param("anioInicio") Short anioInicio, @Param("anioFin") Short anioFin);
    
    /**
     * Cuenta el número de descendientes por persona
     */
    @Query("SELECT COUNT(d) FROM Descendiente d WHERE d.idPersona = :idPersona")
    long countByIdPersona(@Param("idPersona") Long idPersona);
    
    /**
     * Busca descendientes adoptados
     */
    @Query("SELECT d FROM Descendiente d WHERE d.anioAdopcion IS NOT NULL")
    List<Descendiente> findDescendientesAdoptados();
    
    /**
     * Busca descendientes con discapacidad
     */
    @Query("SELECT d FROM Descendiente d WHERE d.codDiscapacidad != 'N'")
    List<Descendiente> findDescendientesConDiscapacidad();
    
    /**
     * Busca descendientes con movilidad reducida
     */
    @Query("SELECT d FROM Descendiente d WHERE d.indMovred = 'S'")
    List<Descendiente> findDescendientesConMovilidadReducida();
}
