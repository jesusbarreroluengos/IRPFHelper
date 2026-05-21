package com.irpf.backend.controller;

import com.irpf.backend.entidades.TaPorcCotiz;
import com.irpf.backend.repository.TaPorcCotizRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Controlador REST para el mantenimiento de la tabla ta_porc_cotiz.
 */
@RestController
@RequestMapping("/api/admin/porc-cotiz")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class TaPorcCotizController {

    @Autowired
    private TaPorcCotizRepository repository;

    @GetMapping("/all")
    public List<TaPorcCotiz> getAll() {
        return repository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody TaPorcCotiz entity) {
        if (repository.findById(entity.getAnio()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un registro para el año " + entity.getAnio());
        }
        if (entity.getPorcentaje() == null) {
            return ResponseEntity.badRequest().body("El porcentaje es obligatorio");
        }
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/update/{anio}")
    public ResponseEntity<?> update(@PathVariable Integer anio, @RequestBody TaPorcCotiz entity) {
        Optional<TaPorcCotiz> existing = repository.findById(anio);
        if (existing.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (entity.getPorcentaje() == null) {
            return ResponseEntity.badRequest().body("El porcentaje es obligatorio");
        }
        existing.get().setPorcentaje(entity.getPorcentaje());
        return ResponseEntity.ok(repository.save(existing.get()));
    }

    @DeleteMapping("/delete/{anio}")
    public ResponseEntity<?> delete(@PathVariable Integer anio) {
        if (repository.findById(anio).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(anio);
        return ResponseEntity.ok("Registro eliminado correctamente");
    }
}
