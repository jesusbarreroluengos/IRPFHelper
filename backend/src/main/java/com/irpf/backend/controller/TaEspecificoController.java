package com.irpf.backend.controller;

import com.irpf.backend.entidades.TaEspecifico;
import com.irpf.backend.entidades.TaEspecificoId;
import com.irpf.backend.entidades.TaEstudios;
import com.irpf.backend.entidades.TaComunidad;
import com.irpf.backend.repository.TaEspecificoRepository;
import com.irpf.backend.repository.TaEstudiosRepository;
import com.irpf.backend.repository.TaComunidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para el mantenimiento de la tabla ta_especifico.
 */
@RestController
@RequestMapping("/api/admin/especifico")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class TaEspecificoController {

    @Autowired
    private TaEspecificoRepository repository;

    @Autowired
    private TaEstudiosRepository estudiosRepository;

    @Autowired
    private TaComunidadRepository comunidadRepository;

    @GetMapping("/all")
    public List<TaEspecifico> getAll() {
        return repository.findAll();
    }

    @GetMapping("/estudios")
    public List<TaEstudios> getEstudios() {
        return estudiosRepository.findAll();
    }

    @GetMapping("/comunidades")
    public List<TaComunidad> getComunidades() {
        return comunidadRepository.findAll();
    }

    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody TaEspecifico entity) {
        TaEspecificoId id = new TaEspecificoId(entity.getAnio(), entity.getCodEstudio(), entity.getIdComunidad());
        if (repository.findById(id).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Ya existe un registro para ese año, estudio y comunidad");
        }
        if (entity.getImporte() == null || entity.getImporte().signum() <= 0) {
            return ResponseEntity.badRequest().body("El importe es obligatorio y debe ser mayor que cero");
        }
        return ResponseEntity.ok(repository.save(entity));
    }

    @PutMapping("/update/{anio}/{codEstudio}/{idComunidad}")
    public ResponseEntity<?> update(@PathVariable Integer anio, @PathVariable String codEstudio,
                                     @PathVariable Integer idComunidad, @RequestBody Map<String, Object> body) {
        TaEspecificoId id = new TaEspecificoId(anio, codEstudio, idComunidad);
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

    @DeleteMapping("/delete/{anio}/{codEstudio}/{idComunidad}")
    public ResponseEntity<?> delete(@PathVariable Integer anio, @PathVariable String codEstudio,
                                     @PathVariable Integer idComunidad) {
        TaEspecificoId id = new TaEspecificoId(anio, codEstudio, idComunidad);
        if (repository.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        repository.deleteById(id);
        return ResponseEntity.ok("Registro eliminado correctamente");
    }
}
