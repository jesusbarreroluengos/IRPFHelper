package com.irpf.backend.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

import com.irpf.backend.entidades.Discapacidad;
import com.irpf.backend.repository.DiscapacidadRepository;

/**
 * Controlador REST para la gestion del catalogo de discapacidades.
 */
@RestController
@RequestMapping("/api/discapacidades")
public class DiscapacidadController {

    @Autowired
    private DiscapacidadRepository discapacidadRepository;

    /**
     * Obtiene todas las discapacidades registradas.
     *
     * @return lista de discapacidades
     */
    @GetMapping("/all")
    public List<Discapacidad> getAllDiscapacidades() {
        return discapacidadRepository.findAll();
    }

    /**
     * Obtiene una discapacidad por su codigo.
     *
     * @param codDiscapacidad codigo de discapacidad
     * @return respuesta con la entidad o 404 si no existe
     */
    @GetMapping("/{codDiscapacidad}")
    public ResponseEntity<Discapacidad> getDiscapacidadByCodigo(@PathVariable String codDiscapacidad) {
        Optional<Discapacidad> discapacidad = discapacidadRepository.findByCodDiscapacidad(codDiscapacidad);
        if (discapacidad.isPresent()) {
            return ResponseEntity.ok(discapacidad.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Crea una discapacidad validando campos obligatorios y duplicidad de codigo.
     *
     * @param data datos de la discapacidad (codDiscapacidad, descDiscapacidad)
     * @return respuesta con el resultado de la operacion
     */
    @PostMapping("/create")
    public ResponseEntity<String> createDiscapacidad(@RequestBody Map<String, String> data) {
        String codDiscapacidad = data.get("codDiscapacidad");
        String descDiscapacidad = data.get("descDiscapacidad");

        if (codDiscapacidad == null || descDiscapacidad == null) {
            return ResponseEntity.badRequest().body("Faltan campos requeridos: codDiscapacidad y descDiscapacidad");
        }

        // Verificar si la discapacidad ya existe
        if (discapacidadRepository.existsByCodDiscapacidad(codDiscapacidad)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe una discapacidad con ese código");
        }

        // Crear nueva discapacidad
        Discapacidad discapacidad = new Discapacidad(codDiscapacidad, descDiscapacidad);
        discapacidadRepository.saveAndFlush(discapacidad);

        return ResponseEntity.ok("Discapacidad creada exitosamente");
    }

    /**
     * Actualiza una discapacidad existente por codigo.
     *
     * @param codDiscapacidad codigo de la discapacidad a actualizar
     * @param data datos a actualizar
     * @return respuesta con el resultado de la operacion
     */
    @PutMapping("/update/{codDiscapacidad}")
    public ResponseEntity<String> updateDiscapacidad(@PathVariable String codDiscapacidad, 
                                                    @RequestBody Map<String, String> data) {
        Optional<Discapacidad> discapacidadOpt = discapacidadRepository.findByCodDiscapacidad(codDiscapacidad);
        if (discapacidadOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Discapacidad no encontrada");
        }

        Discapacidad discapacidad = discapacidadOpt.get();

        // Actualizar descripción si se proporciona
        String descDiscapacidad = data.get("descDiscapacidad");
        if (descDiscapacidad != null && !descDiscapacidad.trim().isEmpty()) {
            discapacidad.setDescDiscapacidad(descDiscapacidad);
        }

        discapacidadRepository.save(discapacidad);
        return ResponseEntity.ok("Discapacidad actualizada exitosamente");
    }

    /**
     * Elimina una discapacidad por su codigo.
     *
     * @param codDiscapacidad codigo de la discapacidad a eliminar
     * @return respuesta con el resultado de la operacion
     */
    @DeleteMapping("/delete/{codDiscapacidad}")
    public ResponseEntity<String> deleteDiscapacidad(@PathVariable String codDiscapacidad) {
        Optional<Discapacidad> discapacidadOpt = discapacidadRepository.findByCodDiscapacidad(codDiscapacidad);
        if (discapacidadOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Discapacidad no encontrada");
        }

        discapacidadRepository.deleteById(codDiscapacidad);
        return ResponseEntity.ok("Discapacidad eliminada exitosamente");
    }
}
