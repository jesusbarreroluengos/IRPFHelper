package com.irpf.backend.controller;

import com.irpf.backend.entidades.TaSexenio;
import com.irpf.backend.entidades.TaSexenioId;
import com.irpf.backend.entidades.TaComunidad;
import com.irpf.backend.repository.TaSexenioRepository;
import com.irpf.backend.repository.TaComunidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el mantenimiento de la tabla ta_sexenio.
 */
@RestController
@RequestMapping("/api/admin/sexenio")
public class TaSexenioController {

    @Autowired
    private TaSexenioRepository repository;

    @Autowired
    private TaComunidadRepository comunidadRepository;

    @GetMapping("/all")
    public List<TaSexenio> getAll() {
        return repository.findAll();
    }

    @GetMapping("/comunidades")
    public List<TaComunidad> getComunidades() {
        return comunidadRepository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody TaSexenio entity) {
        TaSexenioId id = new TaSexenioId(entity.getAnio(), entity.getNumSexenio(), entity.getIdComunidad());
        if (repository.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un registro para ese año, número de sexenio y comunidad");
        }
        if (entity.getImporte() == null || entity.getImporte().signum() <= 0) {
            return ResponseEntity.badRequest().body("El importe es obligatorio y debe ser mayor que cero");
        }
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/update/{anio}/{numSexenio}/{idComunidad}")
    public ResponseEntity<?> update(@PathVariable Integer anio, @PathVariable String numSexenio,
                                     @PathVariable Integer idComunidad, @RequestBody Map<String, Object> body) {
        TaSexenioId id = new TaSexenioId(anio, numSexenio, idComunidad);
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

    @DeleteMapping("/delete/{anio}/{numSexenio}/{idComunidad}")
    public ResponseEntity<?> delete(@PathVariable Integer anio, @PathVariable String numSexenio,
                                     @PathVariable Integer idComunidad) {
        TaSexenioId id = new TaSexenioId(anio, numSexenio, idComunidad);
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok("Registro eliminado correctamente");
    }
}
