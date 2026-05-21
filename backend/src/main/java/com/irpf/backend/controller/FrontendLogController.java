package com.irpf.backend.controller;

import com.irpf.backend.model.FrontendLogRequest;
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
 * Endpoint para recepcion de logs emitidos por el frontend.
 */
@RestController
@RequestMapping("/api/logs")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class FrontendLogController {

    private static final Logger frontendLogger = LogManager.getLogger("FRONTEND_AUDIT");

    /**
     * Registra un evento de log enviado desde el cliente web.
     *
     * @param request payload de log con nivel, mensaje y metadatos de contexto
     * @return respuesta 202 cuando el evento se acepta para registro
     */
    @PostMapping("/frontend")
    public ResponseEntity<Void> registerFrontendLog(@RequestBody FrontendLogRequest request) {
        if (request == null || isBlank(request.message())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        String level = normalizeLevel(request.level());
        String payload = buildPayload(request);

        switch (level) {
            case "ERROR" -> frontendLogger.error(payload);
            case "WARN" -> frontendLogger.warn(payload);
            case "DEBUG" -> frontendLogger.debug(payload);
            default -> frontendLogger.info(payload);
        }

        return ResponseEntity.accepted().build();
    }

    /**
     * Normaliza el nivel de log recibido para limitarlo a valores permitidos.
     *
     * @param level nivel recibido desde frontend
     * @return nivel normalizado compatible con el logger
     */
    private String normalizeLevel(String level) {
        if (isBlank(level)) {
            return "INFO";
        }
        String normalized = level.trim().toUpperCase();
        if (normalized.equals("DEBUG") || normalized.equals("INFO") || normalized.equals("WARN") || normalized.equals("ERROR")) {
            return normalized;
        }
        return "INFO";
    }

    /**
     * Construye una linea de log estructurada con los campos del evento.
     *
     * @param request peticion con datos del log
     * @return cadena lista para su envio al logger
     */
    private String buildPayload(FrontendLogRequest request) {
        return String.format(
                "timestamp=%s type=%s user=%s method=%s status=%s url=%s message=\"%s\" context=\"%s\" stack=\"%s\" userAgent=\"%s\"",
                fallback(request.timestamp()),
                fallback(request.type()),
                fallback(request.username()),
                fallback(request.method()),
                request.statusCode() == null ? "-" : request.statusCode().toString(),
                fallback(request.url()),
                sanitize(request.message()),
                sanitize(request.context()),
                sanitize(request.stackTrace()),
                sanitize(request.userAgent())
        );
    }

    /**
     * Aplica un valor por defecto cuando el dato recibido es nulo o vacio.
     *
     * @param value texto a evaluar
     * @return texto recortado o guion si no hay valor util
     */
    private String fallback(String value) {
        return isBlank(value) ? "-" : value.trim();
    }

    /**
     * Limpia caracteres de control y comillas para evitar entradas multilinea en logs.
     *
     * @param value texto original
     * @return texto saneado para registro seguro
     */
    private String sanitize(String value) {
        if (value == null) {
            return "-";
        }
        return value
                .replaceAll("[\\r\\n\\t]", " ")
                .replace("\"", "'")
                .trim();
    }

    /**
     * Comprueba si un texto es nulo o esta vacio tras recorte.
     *
     * @param value texto a evaluar
     * @return true si no contiene contenido significativo
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
