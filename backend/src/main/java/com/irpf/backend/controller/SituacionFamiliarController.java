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

import com.irpf.backend.entidades.SituacionFamiliar;
import com.irpf.backend.repository.SituacionFamiliarRepository;

@RestController
@RequestMapping("/api/situaciones-familiares")
public class SituacionFamiliarController {

    @Autowired
    private SituacionFamiliarRepository situacionFamiliarRepository;

    /**
     * Obtiene todas las situaciones familiares
     * @return Lista de todas las situaciones familiares
     */
    @GetMapping("/all")
    public List<SituacionFamiliar> getAllSituacionesFamiliares() {
        return situacionFamiliarRepository.findAll();
    }

    /**
     * Obtiene una situación familiar por su código
     * @param codSitfam código de la situación familiar
     * @return ResponseEntity con la situación familiar o mensaje de error
     */
    @GetMapping("/{codSitfam}")
    public ResponseEntity<?> getSituacionFamiliarByCodigo(@PathVariable String codSitfam) {
        Optional<SituacionFamiliar> situacionOpt = situacionFamiliarRepository.findByCodSitfam(codSitfam);
        if (situacionOpt.isPresent()) {
            return ResponseEntity.ok(situacionOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Situación familiar no encontrada");
        }
    }

    /**
     * Crea una nueva situación familiar
     * @param data datos de la situación familiar (codSitfam, descSitfam)
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PostMapping("/create")
    public ResponseEntity<String> createSituacionFamiliar(@RequestBody Map<String, String> data) {
        String codSitfam = data.get("codSitfam");
        String descSitfam = data.get("descSitfam");

        if (codSitfam == null || descSitfam == null) {
            return ResponseEntity.badRequest().body("Faltan campos requeridos: codSitfam y descSitfam");
        }

        // Verificar si la situación familiar ya existe
        if (situacionFamiliarRepository.existsByCodSitfam(codSitfam)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe una situación familiar con ese código");
        }

        // Crear nueva situación familiar
        SituacionFamiliar situacionFamiliar = new SituacionFamiliar(codSitfam, descSitfam);
        situacionFamiliarRepository.saveAndFlush(situacionFamiliar);

        return ResponseEntity.ok("Situación familiar creada exitosamente");
    }

    /**
     * Actualiza una situación familiar existente
     * @param codSitfam código de la situación familiar a actualizar
     * @param data nuevos datos de la situación familiar
     * @return ResponseEntity con mensaje de éxito o error
     */
    @PutMapping("/update/{codSitfam}")
    public ResponseEntity<String> updateSituacionFamiliar(@PathVariable String codSitfam, @RequestBody Map<String, String> data) {
        Optional<SituacionFamiliar> situacionOpt = situacionFamiliarRepository.findByCodSitfam(codSitfam);
        if (situacionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Situación familiar no encontrada");
        }

        SituacionFamiliar situacionFamiliar = situacionOpt.get();
        
        // Actualizar descripción si se proporciona
        if (data.containsKey("descSitfam")) {
            situacionFamiliar.setDescSitfam(data.get("descSitfam"));
        }

        situacionFamiliarRepository.save(situacionFamiliar);
        return ResponseEntity.ok("Situación familiar actualizada exitosamente");
    }

    /**
     * Elimina una situación familiar
     * @param codSitfam código de la situación familiar a eliminar
     * @return ResponseEntity con mensaje de éxito o error
     */
    @DeleteMapping("/delete/{codSitfam}")
    public ResponseEntity<String> deleteSituacionFamiliar(@PathVariable String codSitfam) {
        Optional<SituacionFamiliar> situacionOpt = situacionFamiliarRepository.findByCodSitfam(codSitfam);
        if (situacionOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Situación familiar no encontrada");
        }

        situacionFamiliarRepository.deleteById(codSitfam);
        return ResponseEntity.ok("Situación familiar eliminada exitosamente");
    }
}
