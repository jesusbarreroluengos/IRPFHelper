package com.irpf.backend.controller;

import com.irpf.backend.entidades.ContratoPersona;
import com.irpf.backend.entidades.PuestoTipo;
import com.irpf.backend.model.ApiResponse;
import com.irpf.backend.repository.ContratoPersonaRepository;
import com.irpf.backend.repository.PersonaSimuladaRepository;
import com.irpf.backend.repository.PuestoTipoRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controlador REST para gestion de contratos vinculados a una persona simulada.
 */
@RestController
@RequestMapping("/api/contratos-persona")
public class ContratoPersonaController {

    private static final String MENSAJE_SIN_PERSONA = "Para incluir un nuevo contrato, primero debe crear una persona simulada y puestos tipo para esa persona";

    private static final Logger auditLogger = LogManager.getLogger("BACKEND_AUDIT");

    @Autowired
    private ContratoPersonaRepository contratoPersonaRepository;

    @Autowired
    private PersonaSimuladaRepository personaSimuladaRepository;

    @Autowired
    private PuestoTipoRepository puestoTipoRepository;

    /**
     * Recupera todos los contratos de una persona ordenados por fecha de inicio.
     *
     * @param idPersona identificador de persona simulada
     * @return lista de contratos o mensaje de error si la persona no existe
     */
    @GetMapping("/persona/{idPersona}")
    public ResponseEntity<?> getContratosPorPersona(@PathVariable Long idPersona) {
        if (idPersona == null) {
            return ResponseEntity.badRequest().body(new ApiResponse(false, MENSAJE_SIN_PERSONA));
        }
        if (personaSimuladaRepository.findById(idPersona).isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse(false, MENSAJE_SIN_PERSONA));
        }
        List<ContratoPersona> contratos = contratoPersonaRepository.findByIdPersonaOrderByFechaDesdeAsc(idPersona);
        return ResponseEntity.ok(contratos);
    }

    /**
     * Recupera un contrato por su identificador.
     *
     * @param id identificador del contrato
     * @return contrato encontrado o mensaje de no encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getContratoPorId(@PathVariable Long id) {
        return contratoPersonaRepository.findById(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Contrato no encontrado")));
    }

    /**
     * Crea un contrato nuevo a partir del payload recibido.
     *
     * @param data datos del contrato
     * @return respuesta de exito o error de validacion
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> crearContrato(@RequestBody Map<String, Object> data) {
        try {
            ContratoPersona contrato = construirContrato(data, new ContratoPersona());
            String error = validarContrato(contrato, null);
            if (error != null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, error));
            }
            contratoPersonaRepository.saveAndFlush(contrato);
            auditLogger.info("CREAR_CONTRATO | idContrato={} | idPersona={}",
                    contrato.getIdContratoPersona(), contrato.getIdPersona());
            return ResponseEntity.ok(new ApiResponse(true, "Contrato creado correctamente", contrato));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al crear el contrato: " + e.getMessage()));
        }
    }

    /**
     * Actualiza un contrato existente.
     *
     * @param id identificador del contrato
     * @param data datos a actualizar
     * @return respuesta de exito o error
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> actualizarContrato(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        try {
            Optional<ContratoPersona> existenteOpt = contratoPersonaRepository.findById(id);
            if (existenteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Contrato no encontrado"));
            }
            ContratoPersona existente = existenteOpt.get();
            ContratoPersona contratoActualizado = construirContrato(data, existente);
            contratoActualizado.setIdContratoPersona(id);

            String error = validarContrato(contratoActualizado, id);
            if (error != null) {
                return ResponseEntity.badRequest().body(new ApiResponse(false, error));
            }

            contratoPersonaRepository.save(contratoActualizado);
            return ResponseEntity.ok(new ApiResponse(true, "Contrato actualizado correctamente", contratoActualizado));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al actualizar el contrato: " + e.getMessage()));
        }
    }

    /**
     * Elimina un contrato existente.
     *
     * @param id identificador del contrato
     * @return respuesta de confirmacion o error
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> eliminarContrato(@PathVariable Long id) {
        try {
            Optional<ContratoPersona> existenteOpt = contratoPersonaRepository.findById(id);
            if (existenteOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse(false, "Contrato no encontrado"));
            }
            contratoPersonaRepository.deleteById(id);
            return ResponseEntity.ok(new ApiResponse(true, "Contrato eliminado correctamente"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "Error al eliminar el contrato: " + e.getMessage()));
        }
    }

    /**
     * Construye o completa una entidad de contrato desde un mapa de datos.
     *
     * @param data datos de entrada
     * @param base contrato base a actualizar, o null para crear uno nuevo
     * @return contrato resultante con valores normalizados
     */
    private ContratoPersona construirContrato(Map<String, Object> data, ContratoPersona base) {
        ContratoPersona contrato = base != null ? base : new ContratoPersona();

        if (data.containsKey("idPersona")) {
            contrato.setIdPersona(asLong(data.get("idPersona")));
        }
        if (data.containsKey("idPuestoTipo")) {
            contrato.setIdPuestoTipo(asLong(data.get("idPuestoTipo")));
        }
        if (data.containsKey("fechaDesde")) {
            contrato.setFechaDesde(parseFecha(data.get("fechaDesde")));
        }
        if (data.containsKey("fechaHasta")) {
            contrato.setFechaHasta(parseFecha(data.get("fechaHasta")));
        }

        if (data.containsKey("indVacNoDisfrutadas")) {
            contrato.setIndVacNoDisfrutadas(normalizarIndicador(data.get("indVacNoDisfrutadas")));
        }
        if (contrato.getIndVacNoDisfrutadas() == null || contrato.getIndVacNoDisfrutadas().isBlank()) {
            contrato.setIndVacNoDisfrutadas("N");
        }

        return contrato;
    }

    /**
     * Ejecuta validaciones de integridad y reglas de negocio del contrato.
     *
     * @param contrato contrato a validar
     * @param idEditando identificador actual cuando se edita, null en altas
     * @return mensaje de error si existe problema; null cuando es valido
     */
    private String validarContrato(ContratoPersona contrato, Long idEditando) {
        if (contrato.getIdPersona() == null) {
            return MENSAJE_SIN_PERSONA;
        }
        if (personaSimuladaRepository.findById(contrato.getIdPersona()).isEmpty()) {
            return MENSAJE_SIN_PERSONA;
        }

        if (contrato.getIdPuestoTipo() == null) {
            return "Debe seleccionar un puesto tipo para el contrato";
        }
        Optional<PuestoTipo> puestoOpt = puestoTipoRepository.findById(contrato.getIdPuestoTipo());
        if (puestoOpt.isEmpty()) {
            return "El puesto tipo indicado no existe";
        }
        PuestoTipo puesto = puestoOpt.get();
        if (puesto.getIdPersona() == null || !puesto.getIdPersona().equals(contrato.getIdPersona())) {
            return "El puesto tipo seleccionado no pertenece a la persona simulada";
        }

        if (contrato.getFechaDesde() == null) {
            return "La fecha desde del contrato es obligatoria";
        }
        if (contrato.getFechaHasta() != null && !contrato.getFechaHasta().isAfter(contrato.getFechaDesde())) {
            return "La fecha hasta debe ser posterior a la fecha desde";
        }

        List<ContratoPersona> contratos = contratoPersonaRepository.findByIdPersonaOrderByFechaDesdeAsc(contrato.getIdPersona());
        for (ContratoPersona existente : contratos) {
            if (idEditando != null && existente.getIdContratoPersona().equals(idEditando)) {
                continue;
            }
            LocalDate hastaExistente = existente.getFechaHasta();
            LocalDate hastaNuevo = contrato.getFechaHasta();

            boolean solapaInicio = hastaExistente == null || !hastaExistente.isBefore(contrato.getFechaDesde());
            boolean solapaFin = hastaNuevo == null || !existente.getFechaDesde().isAfter(hastaNuevo);

            if (solapaInicio && solapaFin) {
                return "El contrato se solapa con uno anterior y no está permitido";
            }
        }

        // Normalizar indicador
        contrato.setIndVacNoDisfrutadas(normalizarIndicador(contrato.getIndVacNoDisfrutadas()));
        return null;
    }

    /**
     * Convierte una fecha textual en {@link LocalDate} soportando varios formatos.
     *
     * @param valor valor de entrada
     * @return fecha convertida o null si no es parseable
     */
    private LocalDate parseFecha(Object valor) {
        if (valor == null) return null;
        String texto = valor.toString().trim();
        if (texto.isEmpty()) return null;

        List<DateTimeFormatter> formatos = Arrays.asList(
                DateTimeFormatter.ISO_LOCAL_DATE,
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        );

        for (DateTimeFormatter formatter : formatos) {
            try {
                return LocalDate.parse(texto, formatter);
            } catch (Exception ignored) { }
        }
        return null;
    }

    /**
     * Convierte un valor generico a {@link Long}.
     *
     * @param valor valor a convertir
     * @return numero convertido o null si no tiene formato valido
     */
    private Long asLong(Object valor) {
        if (valor == null) return null;
        try {
            return Long.valueOf(valor.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Normaliza un indicador binario a los valores S/N.
     *
     * @param valor indicador recibido
     * @return S si representa afirmacion; N en caso contrario
     */
    private String normalizarIndicador(Object valor) {
        if (valor == null) return "N";
        String texto = valor.toString().trim().toUpperCase(Locale.ROOT);
        if (texto.startsWith("S")) return "S";
        return "N";
    }
}

