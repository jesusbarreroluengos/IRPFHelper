package com.irpf.backend.controller;

import com.irpf.backend.entidades.AnioEstudioId;
import com.irpf.backend.entidades.TaSueldo;
import com.irpf.backend.repository.TaSueldoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el mantenimiento de la tabla ta_sueldo.
 */
@RestController
@RequestMapping("/api/admin/sueldo")
public class TaSueldoController {

    @Autowired
    private TaSueldoRepository repository;

    @GetMapping("/all")
    public List<TaSueldo> getAll() {
        return repository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody TaSueldo entity) {
        AnioEstudioId id = new AnioEstudioId(entity.getAnio(), entity.getCodEstudio());
        if (repository.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un registro para el año " + entity.getAnio() + " y estudio " + entity.getCodEstudio());
        }
        if (entity.getImporte() == null || entity.getImporte().signum() <= 0) {
            return ResponseEntity.badRequest().body("El importe es obligatorio y debe ser mayor que cero");
        }
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/update/{anio}/{codEstudio}")
    public ResponseEntity<?> update(@PathVariable Integer anio, @PathVariable String codEstudio,
                                     @RequestBody Map<String, Object> body) {
        AnioEstudioId id = new AnioEstudioId(anio, codEstudio);
        return repository.findById(id).map(existing -> {
            if (body.get("importe") == null) {
                return ResponseEntity.badRequest().<Object>body("El importe es obligatorio");
            }
            BigDecimal importe = new BigDecimal(body.get("importe").toString());
            if (importe.signum() <= 0) {
                return ResponseEntity.badRequest().<Object>body("El importe debe ser mayor que cero");
            }
            existing.setImporte(importe);
            return ResponseEntity.<Object>ok(repository.save(existing));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/delete/{anio}/{codEstudio}")
    public ResponseEntity<?> delete(@PathVariable Integer anio, @PathVariable String codEstudio) {
        AnioEstudioId id = new AnioEstudioId(anio, codEstudio);
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok("Registro eliminado correctamente");
    }
}
