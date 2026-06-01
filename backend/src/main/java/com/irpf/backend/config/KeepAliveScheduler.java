package com.irpf.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Scheduler que se auto-pinga periódicamente para evitar que Render
 * duerma la instancia gratuita por inactividad (umbral: 15 minutos).
 * Solo activo con el perfil "prod".
 */
@Component
@Profile("prod")
public class KeepAliveScheduler {

    private static final Logger log = LoggerFactory.getLogger(KeepAliveScheduler.class);

    @Value("${app.self-url}")
    private String selfUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Envía un GET a /irpfhelper/api/ping cada 10 minutos (600 000 ms),
     * con un retardo inicial de 60 s para esperar al arranque completo.
     * El contador de inactividad de Render se reinicia con cada petición
     * entrante, por lo que este intervalo es suficiente bajo el umbral de 15 min.
     */
    @Scheduled(initialDelay = 60_000, fixedDelay = 600_000)
    public void keepAlive() {
        try {
            restTemplate.getForObject(selfUrl + "/irpfhelper/api/ping", String.class);
            log.debug("Keep-alive ping enviado correctamente a {}", selfUrl);
        } catch (Exception e) {
            log.warn("Keep-alive ping fallido: {}", e.getMessage());
        }
    }
}
