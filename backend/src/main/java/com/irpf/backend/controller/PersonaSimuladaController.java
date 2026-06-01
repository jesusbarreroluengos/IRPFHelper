package com.irpf.backend.controller;

import com.irpf.backend.model.ApiResponse;
import com.irpf.backend.entidades.PersonaSimulada;
import com.irpf.backend.repository.AscendienteRepository;
import com.irpf.backend.repository.CalculoIrpfRepository;
import com.irpf.backend.repository.ContratoPersonaRepository;
import com.irpf.backend.repository.DescendienteRepository;
import com.irpf.backend.repository.PersonaSimuladaRepository;
import com.irpf.backend.repository.PuestoTipoRepository;
import com.irpf.backend.repository.SimulacionRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/personas-simuladas")
public class PersonaSimuladaController {

    private static final Logger auditLogger = LogManager.getLogger("BACKEND_AUDIT");

    @Autowired
    private PersonaSimuladaRepository personaSimuladaRepository;

    @Autowired
    private DescendienteRepository descendienteRepository;

    @Autowired
    private AscendienteRepository ascendienteRepository;

    @Autowired
    private ContratoPersonaRepository contratoPersonaRepository;

    @Autowired
    private PuestoTipoRepository puestoTipoRepository;

    @Autowired
    private SimulacionRepository simulacionRepository;

    @Autowired
    private CalculoIrpfRepository calculoIrpfRepository;

    /**
     * Obtener todas las personas simuladas
     */
    @GetMapping("/all")
    public List<PersonaSimulada> getAllPersonasSimuladas() {
        return personaSimuladaRepository.findAll();
    }

    /**
     * Obtener una persona simulada por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<PersonaSimulada> getPersonaSimuladaById(@PathVariable Long id) {
        Optional<PersonaSimulada> personaOpt = personaSimuladaRepository.findById(id);
        return personaOpt.map(persona -> ResponseEntity.ok(persona))
                        .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtener todas las personas simuladas de un usuario específico
     */
    @GetMapping("/usuario/{idUsuario}")
    public List<PersonaSimulada> getPersonasSimuladasByUsuario(@PathVariable Long idUsuario) {
        return personaSimuladaRepository.findByIdUsuario(idUsuario);
    }

    /**
     * Obtener todas las personas simuladas de un usuario con sus relaciones cargadas
     */
    @GetMapping("/usuario/{idUsuario}/con-relaciones")
    public List<PersonaSimulada> getPersonasSimuladasByUsuarioWithRelations(@PathVariable Long idUsuario) {
        return personaSimuladaRepository.findByIdUsuarioWithRelations(idUsuario);
    }

    /**
     * Buscar persona simulada por NIF ficticio
     */
    @GetMapping("/nif/{nifFicticio}")
    public ResponseEntity<PersonaSimulada> getPersonaSimuladaByNif(@PathVariable String nifFicticio) {
        Optional<PersonaSimulada> personaOpt = personaSimuladaRepository.findByNifFicticio(nifFicticio);
        return personaOpt.map(persona -> ResponseEntity.ok(persona))
                        .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crear una nueva persona simulada
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPersonaSimulada(@RequestBody Map<String, Object> data) {
        try {
            // Validar campos requeridos
            if (!data.containsKey("id") || data.get("id") == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "El campo 'id' es requerido"));
            }

            Long id = Long.valueOf(data.get("id").toString());
            String nombre = (String) data.get("nombre");
            
            // Manejar anioNac que puede venir como String o Number
            Object anioNacObj = data.get("anioNac");
            String anioNacStr = null;
            if (anioNacObj != null) {
                if (anioNacObj instanceof String) {
                    anioNacStr = (String) anioNacObj;
                } else if (anioNacObj instanceof Number) {
                    anioNacStr = anioNacObj.toString();
                } else {
                    anioNacStr = anioNacObj.toString();
                }
            }
            
            String codDiscapacidad = (String) data.get("codDiscapacidad");
            String codSitfam = (String) data.get("codSitfam");
            String indCeumelilla = (String) data.getOrDefault("indCeumelilla", "N");
            String codContrato = (String) data.get("codContrato");
            BigDecimal impPensionConyuge = parseBigDecimal(data.get("impPensionConyuge"));
            BigDecimal impPensionHijos   = parseBigDecimal(data.get("impPensionHijos"));
            Integer idComunidad = parseInteger(data.get("idComunidad"));

            if (data.containsKey("idComunidad") && idComunidad == null) {
                return ResponseEntity.badRequest()
                    .body(new ApiResponse(false, "El campo 'idComunidad' no es válido"));
            }

            if (impPensionConyuge == null) {
                impPensionConyuge = BigDecimal.ZERO;
            }
            if (impPensionHijos == null) {
                impPensionHijos = BigDecimal.ZERO;
            }
            if (idComunidad == null) {
                idComunidad = 1;
            }


            // Crear la persona simulada
            PersonaSimulada personaSimulada = new PersonaSimulada();
            personaSimulada.setId(id);
            personaSimulada.setNombre(nombre);
            
            if (anioNacStr != null && !anioNacStr.isEmpty()) {
                personaSimulada.setAnioNac(Short.valueOf(anioNacStr));
            }
            
            personaSimulada.setCodDiscapacidad(codDiscapacidad);
            personaSimulada.setCodSitfam(codSitfam);
            personaSimulada.setIndCeumelilla(indCeumelilla);
            personaSimulada.setCodContrato(codContrato);
            personaSimulada.setImpPensionConyuge(impPensionConyuge);
            personaSimulada.setImpPensionHijos(impPensionHijos);
            personaSimulada.setIdComunidad(idComunidad);

            // Guardar primero para obtener el ID_PERSONA generado
            personaSimuladaRepository.saveAndFlush(personaSimulada);

            // Generar NIF ficticio basado en el ID_PERSONA
            String nifFicticio = generarNifFicticio(personaSimulada.getIdPersona());
            personaSimulada.setNifFicticio(nifFicticio);
            
            // Actualizar con el NIF generado
            personaSimuladaRepository.save(personaSimulada);

            auditLogger.info("CREAR_DOCENTE | idPersona={} | nombre={} | idUsuario={}",
                    personaSimulada.getIdPersona(), personaSimulada.getNombre(), id);
            return ResponseEntity.ok(new ApiResponse(true, "Persona simulada creada exitosamente", personaSimulada));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al crear la persona simulada: " + e.getMessage()));
        }
    }
    
    /**
     * Genera un NIF ficticio basado en el ID de persona
     * Aplica el algoritmo oficial de cálculo de letra del DNI español
     */
    private String generarNifFicticio(Long idPersona) {
        // Secuencia de letras para el cálculo del NIF
        String letras = "TRWAGMYFPDXBNJZSQVHLCKE";
        
        // Convertir el ID a String y rellenar con ceros a la izquierda (8 dígitos)
        String numeroNif = String.format("%08d", idPersona);
        
        // Calcular la letra: resto de dividir el número entre 23
        int resto = idPersona.intValue() % 23;
        char letra = letras.charAt(resto);
        
        // Retornar el NIF completo (8 dígitos + letra)
        return numeroNif + letra;
    }

    /**
     * Actualizar una persona simulada existente
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updatePersonaSimulada(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            Optional<PersonaSimulada> personaOpt = personaSimuladaRepository.findById(id);
            if (personaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Persona simulada no encontrada"));
            }

            PersonaSimulada personaSimulada = personaOpt.get();

            // Actualizar campos si están presentes en el request
            if (data.containsKey("nombre")) {
                personaSimulada.setNombre((String) data.get("nombre"));
            }

            if (data.containsKey("nifFicticio")) {
                String nifFicticio = (String) data.get("nifFicticio");
                if (nifFicticio != null && !nifFicticio.equals(personaSimulada.getNifFicticio())) {
                    if (personaSimuladaRepository.existsByNifFicticio(nifFicticio)) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new ApiResponse(false, "Ya existe una persona simulada con ese NIF ficticio"));
                    }
                }
                personaSimulada.setNifFicticio(nifFicticio);
            }

            if (data.containsKey("anioNac")) {
                Object anioNacObj = data.get("anioNac");
                if (anioNacObj != null) {
                    String anioNacStr;
                    if (anioNacObj instanceof String) {
                        anioNacStr = (String) anioNacObj;
                    } else if (anioNacObj instanceof Number) {
                        anioNacStr = anioNacObj.toString();
                    } else {
                        anioNacStr = anioNacObj.toString();
                    }
                    
                    if (!anioNacStr.isEmpty()) {
                        personaSimulada.setAnioNac(Short.valueOf(anioNacStr));
                    } else {
                        personaSimulada.setAnioNac(null);
                    }
                } else {
                    personaSimulada.setAnioNac(null);
                }
            }

            if (data.containsKey("codDiscapacidad")) {
                personaSimulada.setCodDiscapacidad((String) data.get("codDiscapacidad"));
            }

            if (data.containsKey("codSitfam")) {
                personaSimulada.setCodSitfam((String) data.get("codSitfam"));
            }

            if (data.containsKey("indCeumelilla")) {
                personaSimulada.setIndCeumelilla((String) data.get("indCeumelilla"));
            }

            if (data.containsKey("codContrato")) {
                personaSimulada.setCodContrato((String) data.get("codContrato"));
            }

            if (data.containsKey("impPensionConyuge")) {
                personaSimulada.setImpPensionConyuge(parseBigDecimal(data.get("impPensionConyuge")));
            }
            if (data.containsKey("impPensionHijos")) {
                personaSimulada.setImpPensionHijos(parseBigDecimal(data.get("impPensionHijos")));
            }
            if (data.containsKey("idComunidad")) {
                Integer idComunidad = parseInteger(data.get("idComunidad"));
                if (idComunidad == null) {
                    return ResponseEntity.badRequest()
                        .body(new ApiResponse(false, "El campo 'idComunidad' no es válido"));
                }
                personaSimulada.setIdComunidad(idComunidad);
            }
            personaSimuladaRepository.save(personaSimulada);

            return ResponseEntity.ok(new ApiResponse(true, "Persona simulada actualizada exitosamente", personaSimulada));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al actualizar la persona simulada: " + e.getMessage()));
        }
    }

    /**
     * Eliminar una persona simulada y todos sus datos asociados en cascada
     */
    @Transactional
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deletePersonaSimulada(@PathVariable Long id) {
        try {
            Optional<PersonaSimulada> personaOpt = personaSimuladaRepository.findById(id);
            if (personaOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, "Persona simulada no encontrada"));
            }

            PersonaSimulada persona = personaOpt.get();

            // Borrar registros hijo en orden para respetar las FK
            calculoIrpfRepository.deleteAll(calculoIrpfRepository.findByIdPersona(id));
            simulacionRepository.deleteAll(simulacionRepository.findByIdPersona(id));
            contratoPersonaRepository.deleteAll(contratoPersonaRepository.findByIdPersonaOrderByFechaDesdeAsc(id));
            descendienteRepository.deleteAll(descendienteRepository.findByIdPersona(id));
            ascendienteRepository.deleteAll(ascendienteRepository.findByIdPersona(id));
            puestoTipoRepository.deleteAll(puestoTipoRepository.findByIdPersonaOrderByNomPuestoAsc(id));

            personaSimuladaRepository.deleteById(id);
            auditLogger.info("ELIMINAR_DOCENTE | idPersona={} | nombre={}", id, persona.getNombre());
            return ResponseEntity.ok(new ApiResponse(true, "Persona simulada eliminada exitosamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al eliminar la persona simulada: " + e.getMessage()));
        }
    }

    /**
     * Obtener estadísticas de personas simuladas por usuario
     */
    @GetMapping("/usuario/{idUsuario}/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticasByUsuario(@PathVariable Long idUsuario) {
        try {
            long totalPersonas = personaSimuladaRepository.countByIdUsuario(idUsuario);
            List<PersonaSimulada> personas = personaSimuladaRepository.findByIdUsuario(idUsuario);

            Map<String, Object> estadisticas = Map.of(
                "totalPersonas", totalPersonas,
                "personasConDiscapacidad", personas.stream().filter(p -> p.getCodDiscapacidad() != null).count(),
                "personasCeumelilla", personas.stream().filter(p -> "S".equals(p.getIndCeumelilla())).count(),
                "personasConContrato", personas.stream().filter(p -> p.getCodContrato() != null).count()
            );

            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Integer parseInteger(Object value) {
        if (value == null || value.toString().trim().isEmpty()) {
            return null;
        }
        try {
            return Integer.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

}
