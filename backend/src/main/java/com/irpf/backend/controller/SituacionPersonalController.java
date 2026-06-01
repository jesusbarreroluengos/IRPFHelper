package com.irpf.backend.controller;

import java.util.List;
import java.util.Map;
// import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.irpf.backend.entidades.SituacionPersonal;
import com.irpf.backend.repository.SituacionPersonalRepository;

/**
 * Controlador REST para gestion del catalogo de situaciones personales.
 */
@RestController
@RequestMapping("/api/situacion-personal")
public class SituacionPersonalController {

    @Autowired
    private SituacionPersonalRepository situacionPersonalRepository;

    /**
     * Recupera todas las situaciones personales del catalogo.
     *
     * @return listado completo de situaciones personales
     */
    @GetMapping("/all")
    public List<SituacionPersonal> getAllSituacionesPersonales() {
        return situacionPersonalRepository.findAll();
    }

    /**
     * Obtiene una situacion personal por su codigo.
     *
     * @param codSituper codigo de situacion personal
     * @return entidad encontrada o 404 si no existe
     */
    @GetMapping("/{codSituper}")
    public ResponseEntity<SituacionPersonal> getSituacionPersonalByCodigo(@PathVariable String codSituper) {
        SituacionPersonal situacionPersonal = situacionPersonalRepository.findByCodSituper(codSituper);
        if (situacionPersonal != null) {
            return ResponseEntity.ok(situacionPersonal);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea una nueva situacion personal en base a los datos recibidos.
     *
     * @param data mapa con codigo y descripcion de la situacion
     * @return mensaje de confirmacion o error de validacion/duplicidad
     */
    @PostMapping("/create")
    public ResponseEntity<String> createSituacionPersonal(@RequestBody Map<String, String> data) {
        String codSituper = data.get("codSituper");
        String descSituper = data.get("descSituper");

        if (codSituper == null || descSituper == null) {
            return ResponseEntity.badRequest().body("Faltan campos requeridos");
        }

        // Verificar si ya existe una situación personal con ese código
        if (situacionPersonalRepository.findByCodSituper(codSituper) != null) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe una situación personal con ese código");
        }

        // Crear nueva situación personal
        SituacionPersonal situacionPersonal = new SituacionPersonal(codSituper, descSituper);
        situacionPersonalRepository.saveAndFlush(situacionPersonal);

        return ResponseEntity.ok("Situación personal creada exitosamente");
    }

    /**
     * Actualiza la descripcion de una situacion personal existente.
     *
     * @param codSituper codigo de la situacion a modificar
     * @param data datos de actualizacion
     * @return mensaje de resultado de la operacion
     */
    @PutMapping("/update/{codSituper}")
    public ResponseEntity<String> updateSituacionPersonal(@PathVariable String codSituper, @RequestBody Map<String, String> data) {
        SituacionPersonal situacionPersonal = situacionPersonalRepository.findByCodSituper(codSituper);
        if (situacionPersonal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Situación personal no encontrada");
        }

        String descSituper = data.get("descSituper");
        if (descSituper != null) {
            situacionPersonal.setDescSituper(descSituper);
        }

        situacionPersonalRepository.save(situacionPersonal);
        return ResponseEntity.ok("Situación personal actualizada exitosamente");
    }

    /**
     * Elimina una situacion personal por su codigo.
     *
     * @param codSituper codigo de la situacion a eliminar
     * @return mensaje de confirmacion o 404 si no existe
     */
    @DeleteMapping("/delete/{codSituper}")
    public ResponseEntity<String> deleteSituacionPersonal(@PathVariable String codSituper) {
        SituacionPersonal situacionPersonal = situacionPersonalRepository.findByCodSituper(codSituper);
        if (situacionPersonal == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Situación personal no encontrada");
        }

        situacionPersonalRepository.deleteById(codSituper);
        return ResponseEntity.ok("Situación personal eliminada exitosamente");
    }

    /**
     * Busca situaciones personales por coincidencia parcial de descripcion.
     *
     * @param data mapa con el texto de descripcion a buscar
     * @return lista con coincidencias encontradas
     */
    @GetMapping("/search")
    public ResponseEntity<List<SituacionPersonal>> searchByDescripcion(@RequestBody Map<String, String> data) {
        String descripcion = data.get("descripcion");
        if (descripcion == null || descripcion.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        SituacionPersonal situacionPersonal = situacionPersonalRepository.findByDescSituperContainingIgnoreCase(descripcion);
        if (situacionPersonal != null) {
            return ResponseEntity.ok(List.of(situacionPersonal));
        } else {
            return ResponseEntity.ok(List.of());
        }
    }
}


