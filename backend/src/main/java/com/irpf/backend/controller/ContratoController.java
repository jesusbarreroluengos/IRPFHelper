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

import com.irpf.backend.entidades.Contrato;
import com.irpf.backend.repository.ContratoRepository;

/**
 * Controlador REST para la gestion del catalogo de contratos.
 * Permite consultar, crear, actualizar y eliminar tipos de contrato.
 */
@RestController
@RequestMapping("/api/contratos")
public class ContratoController {

    @Autowired
    private ContratoRepository contratoRepository;

    /**
     * Obtiene el listado completo de contratos disponibles.
     *
     * @return lista de todos los contratos registrados
     */
    @GetMapping("/all")
    public List<Contrato> getAllContratos() {
        return contratoRepository.findAll();
    }

    /**
     * Recupera un contrato por su codigo identificador.
     *
     * @param codContrato codigo del contrato
     * @return respuesta con el contrato encontrado o mensaje de no encontrado
     */
    @GetMapping("/{codContrato}")
    public ResponseEntity<?> getContratoByCodigo(@PathVariable String codContrato) {
        Optional<Contrato> contratoOpt = contratoRepository.findByCodContrato(codContrato);
        if (contratoOpt.isPresent()) {
            return ResponseEntity.ok(contratoOpt.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contrato no encontrado");
        }
    }

    /**
     * Crea un nuevo contrato validando campos obligatorios y duplicidades.
     *
     * @param data datos del contrato (codContrato, descContrato)
     * @return respuesta con mensaje de exito o error de validacion/conflicto
     */
    @PostMapping("/create")
    public ResponseEntity<String> createContrato(@RequestBody Map<String, String> data) {
        String codContrato = data.get("codContrato");
        String descContrato = data.get("descContrato");

        if (codContrato == null || descContrato == null) {
            return ResponseEntity.badRequest().body("Faltan campos requeridos: codContrato y descContrato");
        }

        // Verificar si el contrato ya existe
        if (contratoRepository.existsByCodContrato(codContrato)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya existe un contrato con ese código");
        }

        // Crear nuevo contrato
        Contrato contrato = new Contrato(codContrato, descContrato);
        contratoRepository.saveAndFlush(contrato);

        return ResponseEntity.ok("Contrato creado exitosamente");
    }

    /**
     * Actualiza la informacion de un contrato existente.
     *
     * @param codContrato codigo del contrato a actualizar
     * @param data nuevos datos del contrato
     * @return respuesta con mensaje de exito o error si no existe
     */
    @PutMapping("/update/{codContrato}")
    public ResponseEntity<String> updateContrato(@PathVariable String codContrato, @RequestBody Map<String, String> data) {
        Optional<Contrato> contratoOpt = contratoRepository.findByCodContrato(codContrato);
        if (contratoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contrato no encontrado");
        }

        Contrato contrato = contratoOpt.get();
        
        // Actualizar descripción si se proporciona
        if (data.containsKey("descContrato")) {
            contrato.setDescContrato(data.get("descContrato"));
        }

        contratoRepository.save(contrato);
        return ResponseEntity.ok("Contrato actualizado exitosamente");
    }

    /**
     * Elimina un contrato por su codigo.
     *
     * @param codContrato codigo del contrato a eliminar
     * @return respuesta con mensaje de exito o error si no existe
     */
    @DeleteMapping("/delete/{codContrato}")
    public ResponseEntity<String> deleteContrato(@PathVariable String codContrato) {
        Optional<Contrato> contratoOpt = contratoRepository.findByCodContrato(codContrato);
        if (contratoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Contrato no encontrado");
        }

        contratoRepository.deleteById(codContrato);
        return ResponseEntity.ok("Contrato eliminado exitosamente");
    }
}
