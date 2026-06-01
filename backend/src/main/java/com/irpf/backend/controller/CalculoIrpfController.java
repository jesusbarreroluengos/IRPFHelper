package com.irpf.backend.controller;

import com.irpf.backend.entidades.CalculoIrpf;
import com.irpf.backend.repository.CalculoIrpfRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

/**
 * Controlador REST para gestionar calculos de IRPF almacenados.
 */
@RestController
@RequestMapping("/api/calculos-irpf")
public class CalculoIrpfController {

    private static final Logger logger = LogManager.getLogger(CalculoIrpfController.class);

    @Autowired
    private CalculoIrpfRepository calculoIrpfRepository;

    /**
     * Obtiene todos los calculos de IRPF registrados.
     *
     * @return listado completo de calculos
     */
    @GetMapping("/all")
    public List<CalculoIrpf> getAllCalculos() {
        return calculoIrpfRepository.findAll();
    }

    /**
     * Obtiene un calculo por su identificador.
     *
     * @param id identificador del calculo
     * @return calculo encontrado o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<CalculoIrpf> getCalculoById(@PathVariable Long id) {
        Optional<CalculoIrpf> calculo = calculoIrpfRepository.findById(id);
        return calculo.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Recupera los calculos asociados a un usuario.
     *
     * @param id identificador de usuario
     * @return lista de calculos del usuario
     */
    @GetMapping("/usuario/{id}")
    public List<CalculoIrpf> getCalculosByUsuario(@PathVariable Long id) {
        return calculoIrpfRepository.findByUsuarioId(id);
    }

    /**
     * Recupera los calculos asociados a una persona simulada.
     *
     * @param idPersona identificador de persona simulada
     * @return lista de calculos de la persona
     */
    @GetMapping("/persona/{idPersona}")
    public List<CalculoIrpf> getCalculosByPersona(@PathVariable Long idPersona) {
        return calculoIrpfRepository.findByIdPersona(idPersona);
    }

    /**
     * Recupera calculos de un ejercicio fiscal concreto.
     *
     * @param ejercicioFiscal ejercicio fiscal consultado
     * @return lista de calculos del ejercicio
     */
    @GetMapping("/ejercicio/{ejercicioFiscal}")
    public List<CalculoIrpf> getCalculosByEjercicio(@PathVariable Short ejercicioFiscal) {
        return calculoIrpfRepository.findByEjercicioFiscal(ejercicioFiscal);
    }

    /**
     * Recupera calculos filtrados por usuario y ejercicio fiscal.
     *
     * @param id identificador de usuario
     * @param ejercicioFiscal ejercicio fiscal consultado
     * @return lista de calculos que cumplen ambos filtros
     */
    @GetMapping("/usuario/{id}/ejercicio/{ejercicioFiscal}")
    public List<CalculoIrpf> getCalculosByUsuarioAndEjercicio(@PathVariable Long id, @PathVariable Short ejercicioFiscal) {
        return calculoIrpfRepository.findByUsuarioIdAndEjercicioFiscal(id, ejercicioFiscal);
    }

    /**
     * Recupera calculos filtrados por persona simulada y ejercicio fiscal.
     *
     * @param idPersona identificador de persona simulada
     * @param ejercicioFiscal ejercicio fiscal consultado
     * @return lista de calculos que cumplen ambos filtros
     */
    @GetMapping("/persona/{idPersona}/ejercicio/{ejercicioFiscal}")
    public List<CalculoIrpf> getCalculosByPersonaAndEjercicio(@PathVariable Long idPersona, @PathVariable Short ejercicioFiscal) {
        return calculoIrpfRepository.findByIdPersonaAndEjercicioFiscal(idPersona, ejercicioFiscal);
    }

    /**
     * Recupera el ultimo calculo disponible para una persona y ejercicio fiscal.
     *
     * @param idPersona identificador de persona simulada
     * @param ejercicioFiscal ejercicio fiscal consultado
     * @return calculo mas reciente o 404 si no existe
     */
    @GetMapping("/persona/{idPersona}/ejercicio/{ejercicioFiscal}/ultimo")
    public ResponseEntity<CalculoIrpf> getUltimoCalculoByPersonaAndEjercicio(@PathVariable Long idPersona, @PathVariable Short ejercicioFiscal) {
        Optional<CalculoIrpf> calculo = calculoIrpfRepository.findUltimoCalculoByPersonaAndEjercicio(idPersona, ejercicioFiscal);
        return calculo.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Crea un nuevo registro de calculo IRPF a partir de un payload dinamico.
     *
     * @param data mapa con datos del calculo y sus importes
     * @return mensaje de exito o error de validacion
     */
    @PostMapping("/create")
    public ResponseEntity<String> createCalculo(@RequestBody Map<String, Object> data) {
        try {
            CalculoIrpf calculo = new CalculoIrpf();

            // Campos obligatorios
            Long id = Long.valueOf(data.get("id").toString());
            Long idPersona = Long.valueOf(data.get("idPersona").toString());
            Short ejercicioFiscal = Short.valueOf(data.get("ejercicioFiscal").toString());

            calculo.setId(id);
            calculo.setIdPersona(idPersona);
            calculo.setEjercicioFiscal(ejercicioFiscal);

            // Campos con valores por defecto
            calculo.setImpPensionConyuge(getBigDecimalValue(data, "impPensionConyuge"));
            calculo.setImpPensionHijos(getBigDecimalValue(data, "impPensionHijos"));
            calculo.setPorcentajeIrpf(getBigDecimalValue(data, "porcentajeIrpf"));
            calculo.setImpCobradoBruto(getBigDecimalValue(data, "impCobradoBruto"));
            calculo.setImpRetenidoIrpf(getBigDecimalValue(data, "impRetenidoIrpf"));
            calculo.setImpRetrinidoGastos(getBigDecimalValue(data, "impRetrinidoGastos"));
            calculo.setImpPendienteBruto(getBigDecimalValue(data, "impPendienteBruto"));
            calculo.setImpPendienteGastos(getBigDecimalValue(data, "impPendienteGastos"));
            calculo.setImpPendienteRetenciones(getBigDecimalValue(data, "impPendienteRetenciones"));

            calculoIrpfRepository.saveAndFlush(calculo);

            return ResponseEntity.ok("Cálculo IRPF creado exitosamente");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error al crear el cálculo: " + e.getMessage());
        }
    }

    /**
     * Actualiza un calculo existente con los campos presentes en la peticion.
     *
     * @param id identificador del calculo a actualizar
     * @param data mapa con campos a modificar
     * @return mensaje de resultado de la operacion
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<String> updateCalculo(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Optional<CalculoIrpf> calculoOpt = calculoIrpfRepository.findById(id);
        if (calculoOpt.isEmpty()) {
            logger.warn("Actualizacion de calculo IRPF rechazada: id={} no encontrado", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cálculo no encontrado");
        }

        try {
            CalculoIrpf calculo = calculoOpt.get();

            // Actualizar campos si están presentes
            if (data.containsKey("ejercicioFiscal")) {
                calculo.setEjercicioFiscal(Short.valueOf(data.get("ejercicioFiscal").toString()));
            }
            if (data.containsKey("impPensionConyuge")) {
                calculo.setImpPensionConyuge(getBigDecimalValue(data, "impPensionConyuge"));
            }
            if (data.containsKey("impPensionHijos")) {
                calculo.setImpPensionHijos(getBigDecimalValue(data, "impPensionHijos"));
            }
            if (data.containsKey("porcentajeIrpf")) {
                calculo.setPorcentajeIrpf(getBigDecimalValue(data, "porcentajeIrpf"));
            }
            if (data.containsKey("impCobradoBruto")) {
                calculo.setImpCobradoBruto(getBigDecimalValue(data, "impCobradoBruto"));
            }
            if (data.containsKey("impRetenidoIrpf")) {
                calculo.setImpRetenidoIrpf(getBigDecimalValue(data, "impRetenidoIrpf"));
            }
            if (data.containsKey("impRetrinidoGastos")) {
                calculo.setImpRetrinidoGastos(getBigDecimalValue(data, "impRetrinidoGastos"));
            }
            if (data.containsKey("impPendienteBruto")) {
                calculo.setImpPendienteBruto(getBigDecimalValue(data, "impPendienteBruto"));
            }
            if (data.containsKey("impPendienteGastos")) {
                calculo.setImpPendienteGastos(getBigDecimalValue(data, "impPendienteGastos"));
            }
            if (data.containsKey("impPendienteRetenciones")) {
                calculo.setImpPendienteRetenciones(getBigDecimalValue(data, "impPendienteRetenciones"));
            }

            calculoIrpfRepository.save(calculo);
            logger.info("Calculo IRPF actualizado: id={}, idPersona={}, ejercicioFiscal={}, porcentajeIrpf={}",
                    calculo.getId(), calculo.getIdPersona(), calculo.getEjercicioFiscal(), calculo.getPorcentajeIrpf());

            return ResponseEntity.ok("Cálculo IRPF actualizado exitosamente");
        } catch (Exception e) {
            logger.error("Error al actualizar calculo IRPF id={}: {}", id, e.getMessage());
            return ResponseEntity.badRequest().body("Error al actualizar el cálculo: " + e.getMessage());
        }
    }

    /**
     * Elimina un calculo por su identificador.
     *
     * @param id identificador del calculo
     * @return mensaje de confirmacion o 404 si no existe
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteCalculo(@PathVariable Long id) {
        Optional<CalculoIrpf> calculoOpt = calculoIrpfRepository.findById(id);
        if (calculoOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cálculo no encontrado");
        }

        calculoIrpfRepository.deleteById(id);
        return ResponseEntity.ok("Cálculo IRPF eliminado exitosamente");
    }

    /**
     * Convierte un valor del payload a {@link BigDecimal} de forma tolerante a formatos.
     *
     * @param data mapa con los datos recibidos
     * @param key clave del campo a convertir
     * @return valor convertido o cero cuando no es valido
     */
    private BigDecimal getBigDecimalValue(Map<String, Object> data, String key) {
        if (data.containsKey(key) && data.get(key) != null) {
            Object value = data.get(key);
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else if (value instanceof String) {
                try {
                    return new BigDecimal((String) value);
                } catch (NumberFormatException e) {
                    return BigDecimal.ZERO;
                }
            }
        }
        return BigDecimal.ZERO;
    }
}
