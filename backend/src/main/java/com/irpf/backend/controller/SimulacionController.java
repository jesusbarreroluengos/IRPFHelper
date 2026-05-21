package com.irpf.backend.controller;

import com.irpf.backend.model.ApiResponse;
import com.irpf.backend.service.SimulacionRetribucionesService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para ejecutar simulaciones de retribuciones e IRPF.
 */
@RestController
@RequestMapping("/api/simulacion")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class SimulacionController {

    private static final Logger logger = LogManager.getLogger(SimulacionController.class);
    private static final Logger auditLogger = LogManager.getLogger("BACKEND_AUDIT");
    private final SimulacionRetribucionesService simulacionRetribucionesService;

    public SimulacionController(SimulacionRetribucionesService simulacionRetribucionesService) {
        this.simulacionRetribucionesService = simulacionRetribucionesService;
    }

    /**
     * Ejecuta una simulacion para una persona y ejercicio a partir de los datos enviados.
     *
     * @param payload datos de entrada con identificador de persona, ejercicio e importes abonados
     * @return respuesta con resultado de la simulacion o detalle de error de validacion
     */
    @PostMapping("/ejecutar")
    public ResponseEntity<ApiResponse> ejecutarSimulacion(@RequestBody Map<String, Object> payload) {
        try {
            Long idPersona = parseLong(payload.get("idPersona"));
            Integer ejercicio = parseInt(payload.get("ejercicio"));
            BigDecimal impBrutoAbonado = parseBigDecimal(payload.get("impBrutoAbonado"));
            BigDecimal impRetenciones = parseBigDecimal(payload.get("impRetencionesPracticadas"));
            BigDecimal impGastos = parseBigDecimal(payload.get("impGastosRealizados"));
            LocalDate fechaHasta = parseFecha(payload.get("fechaHastaAbonado"));

            validarOpcionalPositivo(impBrutoAbonado, "Importe bruto abonado");
            validarOpcionalPositivo(impRetenciones, "Importe retenciones IRPF practicadas");
            validarOpcionalPositivo(impGastos, "Importe Gastos Abonados");

            ApiResponse response = simulacionRetribucionesService.simular(
                    idPersona, ejercicio, impBrutoAbonado, impRetenciones, impGastos, fechaHasta);
            logger.info("Solicitud de simulacion procesada: idPersona={}, ejercicio={}", idPersona, ejercicio);
            auditLogger.info("SIMULACION | idPersona={} | ejercicio={}", idPersona, ejercicio);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            logger.warn("Simulacion rechazada por validacion: {}", e.getMessage());
            return ResponseEntity.badRequest().body(new ApiResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("Error inesperado en simulacion: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse(false, "No se pudo ejecutar la simulación: " + e.getMessage()));
        }
    }

    /**
     * Valida que un importe opcional, si existe, sea positivo.
     *
     * @param valor importe recibido
     * @param nombreCampo nombre del campo para mensajes de error
     */
    private void validarOpcionalPositivo(BigDecimal valor, String nombreCampo) {
        if (valor != null && valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(nombreCampo + " debe ser mayor que cero");
        }
    }

    /**
     * Convierte un valor genérico a {@link Long}.
     *
     * @param o valor de entrada
     * @return numero convertido o null si no se recibe valor
     */
    private Long parseLong(Object o) {
        if (o == null) return null;
        try {
            return Long.valueOf(o.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parámetro inválido");
        }
    }

    /**
     * Convierte un valor genérico a {@link Integer}.
     *
     * @param o valor de entrada
     * @return numero convertido o null si no se recibe valor
     */
    private Integer parseInt(Object o) {
        if (o == null) return null;
        try {
            return Integer.valueOf(o.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parámetro inválido");
        }
    }

    /**
     * Convierte un valor genérico a {@link BigDecimal}.
     *
     * @param o valor de entrada
     * @return importe convertido o null si el valor viene vacio
     */
    private BigDecimal parseBigDecimal(Object o) {
        if (o == null || o.toString().isBlank()) return null;
        try {
            return new BigDecimal(o.toString());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Parámetro numérico inválido");
        }
    }

    /**
     * Convierte un valor genérico a {@link LocalDate} en formato ISO.
     *
     * @param o valor de entrada
     * @return fecha convertida o null si no se informa
     */
    private LocalDate parseFecha(Object o) {
        if (o == null || o.toString().isBlank()) return null;
        try {
            return LocalDate.parse(o.toString());
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("Fecha inválida");
        }
    }
}
