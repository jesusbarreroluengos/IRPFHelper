package com.irpf.backend.controller;

import com.irpf.backend.entidades.Descendiente;
import com.irpf.backend.repository.DescendienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controlador REST para gestion de descendientes de una persona simulada.
 */
@RestController
@RequestMapping("/api/descendientes")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class DescendienteController {

    @Autowired
    private DescendienteRepository descendienteRepository;

    /**
     * Obtiene el listado completo de descendientes.
     *
     * @return lista de descendientes
     */
    @GetMapping("/all")
    public List<Descendiente> getAllDescendientes() {
        return descendienteRepository.findAll();
    }

    /**
     * Obtiene un descendiente por identificador.
     *
     * @param id identificador de descendiente
     * @return entidad encontrada o 404
     */
    @GetMapping("/{id}")
    public ResponseEntity<Descendiente> getDescendienteById(@PathVariable Long id) {
        Optional<Descendiente> descendienteOpt = descendienteRepository.findById(id);
        return descendienteOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene descendientes vinculados a una persona.
     *
     * @param idPersona identificador de persona simulada
     * @return lista de descendientes de la persona
     */
    @GetMapping("/persona/{idPersona}")
    public List<Descendiente> getDescendientesByPersona(@PathVariable Long idPersona) {
        return descendienteRepository.findByIdPersona(idPersona);
    }

    /**
     * Obtiene descendientes de una persona con relaciones precargadas.
     *
     * @param idPersona identificador de persona simulada
     * @return lista con relaciones necesarias para su uso posterior
     */
    @GetMapping("/persona/{idPersona}/con-relaciones")
    public List<Descendiente> getDescendientesByPersonaWithRelations(@PathVariable Long idPersona) {
        return descendienteRepository.findByIdPersonaWithRelations(idPersona);
    }

    /**
     * Filtra descendientes por anio de nacimiento.
     *
     * @param anioNac anio de nacimiento
     * @return lista filtrada
     */
    @GetMapping("/anio-nacimiento/{anioNac}")
    public List<Descendiente> getDescendientesByAnioNac(@PathVariable Short anioNac) {
        return descendienteRepository.findByAnioNac(anioNac);
    }

    /**
     * Filtra descendientes por rango de anio de nacimiento.
     *
     * @param anioInicio anio inicial del rango
     * @param anioFin anio final del rango
     * @return lista filtrada
     */
    @GetMapping("/anio-nacimiento/{anioInicio}/{anioFin}")
    public List<Descendiente> getDescendientesByAnioNacBetween(@PathVariable Short anioInicio, @PathVariable Short anioFin) {
        return descendienteRepository.findByAnioNacBetween(anioInicio, anioFin);
    }

    /**
     * Recupera descendientes con anio de adopcion informado.
     *
     * @return lista de descendientes adoptados
     */
    @GetMapping("/adoptados")
    public List<Descendiente> getDescendientesAdoptados() {
        return descendienteRepository.findDescendientesAdoptados();
    }

    /**
     * Recupera descendientes con codigos de discapacidad distintos de N.
     *
     * @return lista de descendientes con discapacidad
     */
    @GetMapping("/con-discapacidad")
    public List<Descendiente> getDescendientesConDiscapacidad() {
        return descendienteRepository.findDescendientesConDiscapacidad();
    }

    /**
     * Recupera descendientes con movilidad reducida.
     *
     * @return lista de descendientes con indicador de movilidad reducida
     */
    @GetMapping("/movilidad-reducida")
    public List<Descendiente> getDescendientesConMovilidadReducida() {
        return descendienteRepository.findDescendientesConMovilidadReducida();
    }

    /**
     * Crea un descendiente validando campos obligatorios y reglas de negocio.
     *
     * @param data datos del descendiente
     * @return mensaje de resultado de la creacion
     */
    @PostMapping("/create")
    public ResponseEntity<String> createDescendiente(@RequestBody Map<String, Object> data) {
        try {
            ValidacionDescendiente validacion = validarYConvertir(data);
            if (validacion.error() != null) {
                return ResponseEntity.badRequest().body(validacion.error());
            }

            Descendiente descendiente = new Descendiente();
            aplicarDatos(descendiente, validacion);

            descendienteRepository.saveAndFlush(descendiente);
            return ResponseEntity.ok("Descendiente creado exitosamente");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(mapearErrorIntegridad(e));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al crear el descendiente: " + e.getMessage());
        }
    }

    /**
     * Actualiza un descendiente existente tras validar los datos recibidos.
     *
     * @param id identificador del descendiente a actualizar
     * @param data datos a modificar
     * @return mensaje de resultado de la actualizacion
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateDescendiente(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            Optional<Descendiente> descendienteOpt = descendienteRepository.findById(id);
            if (descendienteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Descendiente no encontrado");
            }

            ValidacionDescendiente validacion = validarYConvertir(data);
            if (validacion.error() != null) {
                return ResponseEntity.badRequest().body(validacion.error());
            }

            Descendiente descendiente = descendienteOpt.get();
            aplicarDatos(descendiente, validacion);

            descendienteRepository.saveAndFlush(descendiente);
            return ResponseEntity.ok("Descendiente actualizado exitosamente");
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(mapearErrorIntegridad(e));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al actualizar el descendiente: " + e.getMessage());
        }
    }

    /**
     * Elimina un descendiente por identificador.
     *
     * @param id identificador del descendiente
     * @return mensaje de confirmacion o error
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteDescendiente(@PathVariable Long id) {
        try {
            Optional<Descendiente> descendienteOpt = descendienteRepository.findById(id);
            if (descendienteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Descendiente no encontrado");
            }

            descendienteRepository.deleteById(id);
            return ResponseEntity.ok("Descendiente eliminado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error al eliminar el descendiente: " + e.getMessage());
        }
    }

    /**
     * Obtiene estadisticas agregadas de descendientes de una persona.
     *
     * @param idPersona identificador de persona simulada
     * @return mapa con metricas calculadas
     */
    @GetMapping("/persona/{idPersona}/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticasByPersona(@PathVariable Long idPersona) {
        try {
            long totalDescendientes = descendienteRepository.countByIdPersona(idPersona);
            List<Descendiente> descendientes = descendienteRepository.findByIdPersona(idPersona);

            Map<String, Object> estadisticas = Map.of(
                    "totalDescendientes", totalDescendientes,
                    "descendientesAdoptados", descendientes.stream().filter(d -> d.getAnioAdopcion() != null).count(),
                    "descendientesConDiscapacidad", descendientes.stream().filter(d -> !"N".equals(d.getCodDiscapacidad())).count(),
                    "descendientesConMovilidadReducida", descendientes.stream().filter(d -> "S".equals(d.getIndMovred())).count(),
                    "descendientesPorEntero", descendientes.stream().filter(d -> "S".equals(d.getIndPorentero())).count()
            );

            return ResponseEntity.ok(estadisticas);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error al obtener estadísticas: " + e.getMessage()));
        }
    }

    /**
     * Valida campos obligatorios del payload y convierte sus tipos.
     *
     * @param data datos entrantes del descendiente
     * @return resultado de conversion con error si la entrada no es valida
     */
    private ValidacionDescendiente validarYConvertir(Map<String, Object> data) {
        if (!data.containsKey("idPersona") || data.get("idPersona") == null) {
            return ValidacionDescendiente.error("El campo 'idPersona' es requerido");
        }
        if (!data.containsKey("anioNac") || data.get("anioNac") == null || data.get("anioNac").toString().isBlank()) {
            return ValidacionDescendiente.error("El campo 'anioNac' es requerido");
        }
        if (!data.containsKey("indPorentero") || data.get("indPorentero") == null) {
            return ValidacionDescendiente.error("El campo 'indPorentero' es requerido");
        }
        if (!data.containsKey("codDiscapacidad") || data.get("codDiscapacidad") == null) {
            return ValidacionDescendiente.error("El campo 'codDiscapacidad' es requerido");
        }
        if (!data.containsKey("indMovred") || data.get("indMovred") == null) {
            return ValidacionDescendiente.error("El campo 'indMovred' es requerido");
        }

        try {
            Long idPersona = Long.valueOf(data.get("idPersona").toString());
            Short anioNac = Short.valueOf(data.get("anioNac").toString());
            Short anioAdopcion = obtenerShortNullable(data.get("anioAdopcion"));
            String indPorentero = data.get("indPorentero").toString();
            String codDiscapacidad = data.get("codDiscapacidad").toString();
            String indMovred = data.get("indMovred").toString();

            if (anioAdopcion != null && anioAdopcion < anioNac) {
                return ValidacionDescendiente.error("El campo 'anioAdopcion' debe ser mayor o igual que el campo 'anioNac'");
            }

            return new ValidacionDescendiente(idPersona, anioNac, anioAdopcion, indPorentero, codDiscapacidad, indMovred, null);
        } catch (NumberFormatException e) {
            return ValidacionDescendiente.error("Los campos numéricos del descendiente no tienen un formato válido");
        }
    }

    /**
     * Convierte un valor opcional a {@link Short} devolviendo null si viene vacio.
     *
     * @param value valor a convertir
     * @return valor short o null
     */
    private Short obtenerShortNullable(Object value) {
        if (value == null) {
            return null;
        }

        String texto = value.toString().trim();
        if (texto.isEmpty()) {
            return null;
        }

        return Short.valueOf(texto);
    }

    /**
     * Copia en la entidad los datos ya validados.
     *
     * @param descendiente entidad destino
     * @param validacion datos validados y convertidos
     */
    private void aplicarDatos(Descendiente descendiente, ValidacionDescendiente validacion) {
        descendiente.setIdPersona(validacion.idPersona());
        descendiente.setAnioNac(validacion.anioNac());
        descendiente.setAnioAdopcion(validacion.anioAdopcion());
        descendiente.setIndPorentero(validacion.indPorentero());
        descendiente.setCodDiscapacidad(validacion.codDiscapacidad());
        descendiente.setIndMovred(validacion.indMovred());
    }

    /**
     * Traduce errores tecnicos de integridad a mensajes de negocio comprensibles.
     *
     * @param e excepcion de integridad lanzada por persistencia
     * @return mensaje de error adaptado para el cliente
     */
    private String mapearErrorIntegridad(DataIntegrityViolationException e) {
        String mensaje = e.getMostSpecificCause() != null ? e.getMostSpecificCause().getMessage() : e.getMessage();

        if (mensaje != null && mensaje.contains("Solo puede informar el campo 'anioNac' o el campo 'anioAdopcion', pero no ambos")) {
            return "La base de datos sigue aplicando la validación antigua de descendientes. Actualice manualmente la restricción de la tabla.";
        }

        return "Error de integridad al guardar el descendiente: " + mensaje;
    }

    private record ValidacionDescendiente(
            Long idPersona,
            Short anioNac,
            Short anioAdopcion,
            String indPorentero,
            String codDiscapacidad,
            String indMovred,
            String error
    ) {
        private static ValidacionDescendiente error(String mensaje) {
            return new ValidacionDescendiente(null, null, null, null, null, null, mensaje);
        }
    }
}
