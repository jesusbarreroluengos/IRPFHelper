package com.irpf.backend.controller;

import com.irpf.backend.entidades.Ascendiente;
import com.irpf.backend.repository.AscendienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/ascendientes")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AscendienteController {

    @Autowired
    private AscendienteRepository ascendienteRepository;

    /**
     * Obtener todos los ascendientes
     */
    @GetMapping("/all")
    public List<Ascendiente> getAllAscendientes() {
        return ascendienteRepository.findAll();
    }

    /**
     * Obtener un ascendiente por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<Ascendiente> getAscendienteById(@PathVariable Long id) {
        Optional<Ascendiente> ascendienteOpt = ascendienteRepository.findById(id);
        return ascendienteOpt.map(ascendiente -> ResponseEntity.ok(ascendiente))
                            .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtener todos los ascendientes de una persona específica
     */
    @GetMapping("/persona/{idPersona}")
    public List<Ascendiente> getAscendientesByPersona(@PathVariable Long idPersona) {
        return ascendienteRepository.findByIdPersona(idPersona);
    }

    /**
     * Obtener todos los ascendientes de una persona con sus relaciones cargadas
     */
    @GetMapping("/persona/{idPersona}/con-relaciones")
    public List<Ascendiente> getAscendientesByPersonaWithRelations(@PathVariable Long idPersona) {
        return ascendienteRepository.findByIdPersonaWithDiscapacidad(idPersona);
    }

    /**
     * Buscar ascendientes por año de nacimiento
     */
    @GetMapping("/anio-nacimiento/{anioNac}")
    public List<Ascendiente> getAscendientesByAnioNac(@PathVariable Short anioNac) {
        return ascendienteRepository.findByAnioNac(anioNac);
    }

    /**
     * Buscar ascendientes con discapacidad
     */
    @GetMapping("/con-discapacidad")
    public List<Ascendiente> getAscendientesConDiscapacidad() {
        return ascendienteRepository.findByCodDiscapacidad("S");
    }

    /**
     * Buscar ascendientes con indicador de compartido específico
     */
    @GetMapping("/compartido/{indCompartido}")
    public List<Ascendiente> getAscendientesByIndCompartido(@PathVariable Short indCompartido) {
        return ascendienteRepository.findByIndCompartido(indCompartido);
    }

    /**
     * Crear un nuevo ascendiente
     */
    @PostMapping("/create")
    public ResponseEntity<String> createAscendiente(@RequestBody Map<String, Object> data) {
        try {
            // Validar campos requeridos
            if (!data.containsKey("idPersona") || data.get("idPersona") == null) {
                return ResponseEntity.badRequest().body("El campo 'idPersona' es requerido");
            }
            if (!data.containsKey("anioNac") || data.get("anioNac") == null) {
                return ResponseEntity.badRequest().body("El campo 'anioNac' es requerido");
            }
            if (!data.containsKey("codDiscapacidad") || data.get("codDiscapacidad") == null) {
                return ResponseEntity.badRequest().body("El campo 'codDiscapacidad' es requerido");
            }
            if (!data.containsKey("indCompartido") || data.get("indCompartido") == null) {
                return ResponseEntity.badRequest().body("El campo 'indCompartido' es requerido");
            }

            Long idPersona = Long.valueOf(data.get("idPersona").toString());
            Short anioNac = Short.valueOf(data.get("anioNac").toString());
            String codDiscapacidad = (String) data.get("codDiscapacidad");
            Short indCompartido = Short.valueOf(data.get("indCompartido").toString());

            // Crear el ascendiente
            Ascendiente ascendiente = new Ascendiente();
            ascendiente.setIdPersona(idPersona);
            ascendiente.setAnioNac(anioNac);
            ascendiente.setCodDiscapacidad(codDiscapacidad);
            ascendiente.setIndCompartido(indCompartido);

            ascendienteRepository.saveAndFlush(ascendiente);

            return ResponseEntity.ok("Ascendiente creado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el ascendiente: " + e.getMessage());
        }
    }

    /**
     * Actualizar un ascendiente existente
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateAscendiente(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            Optional<Ascendiente> ascendienteOpt = ascendienteRepository.findById(id);
            if (ascendienteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ascendiente no encontrado");
            }

            Ascendiente ascendiente = ascendienteOpt.get();

            // Actualizar campos si están presentes en el request
            if (data.containsKey("idPersona")) {
                ascendiente.setIdPersona(Long.valueOf(data.get("idPersona").toString()));
            }

            if (data.containsKey("anioNac")) {
                Object anioNacObj = data.get("anioNac");
                if (anioNacObj != null) {
                    ascendiente.setAnioNac(Short.valueOf(anioNacObj.toString()));
                } else {
                    ascendiente.setAnioNac(null);
                }
            }

            if (data.containsKey("codDiscapacidad")) {
                ascendiente.setCodDiscapacidad((String) data.get("codDiscapacidad"));
            }

            if (data.containsKey("indCompartido")) {
                Object numHijosobj = data.get("indCompartido");
                if (numHijosobj == null) {
                    ascendiente.setIndCompartido(null);
                } else {          
                ascendiente.setIndCompartido(Short.valueOf(numHijosobj.toString()));
                }
            }

            ascendienteRepository.saveAndFlush(ascendiente);

            return ResponseEntity.ok("Ascendiente actualizado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el ascendiente: " + e.getMessage());
        }
    }

    /**
     * Eliminar un ascendiente
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteAscendiente(@PathVariable Long id) {
        try {
            Optional<Ascendiente> ascendienteOpt = ascendienteRepository.findById(id);
            if (ascendienteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Ascendiente no encontrado");
            }

            ascendienteRepository.deleteById(id);
            return ResponseEntity.ok("Ascendiente eliminado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el ascendiente: " + e.getMessage());
        }
    }

    /**
     * Contar ascendientes de una persona
     */
    @GetMapping("/persona/{idPersona}/count")
    public ResponseEntity<Long> countAscendientesByPersona(@PathVariable Long idPersona) {
        long count = ascendienteRepository.countByIdPersona(idPersona);
        return ResponseEntity.ok(count);
    }

    /**
     * Buscar ascendientes de una persona con discapacidad específica
     */
    @GetMapping("/persona/{idPersona}/discapacidad/{codDiscapacidad}")
    public List<Ascendiente> getAscendientesByPersonaAndDiscapacidad(@PathVariable Long idPersona, @PathVariable String codDiscapacidad) {
        return ascendienteRepository.findByIdPersonaAndCodDiscapacidad(idPersona, codDiscapacidad);
    }

    /**
     * Buscar ascendientes de una persona con indicador de compartido específico
     */
    @GetMapping("/persona/{idPersona}/compartido/{indCompartido}")
    public List<Ascendiente> getAscendientesByPersonaAndIndCompartido(@PathVariable Long idPersona, @PathVariable Short indCompartido) {
        return ascendienteRepository.findByIdPersonaAndIndCompartido(idPersona, indCompartido);
    }
}
