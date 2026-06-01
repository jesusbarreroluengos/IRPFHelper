package com.irpf.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint ligero para verificar que el servicio está activo.
 * Lo usa el propio scheduler de keep-alive para evitar que Render
 * ponga en reposo la instancia gratuita por inactividad.
 */
@RestController
@RequestMapping("/api")
public class PingController {

    @GetMapping("/ping")
    public ResponseEntity<String> ping() {
        return ResponseEntity.ok("pong");
    }
}
