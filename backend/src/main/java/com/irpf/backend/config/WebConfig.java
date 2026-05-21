package com.irpf.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.lang.NonNull;

/**
 * Configuracion web para servir recursos estaticos necesarios por el frontend.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Registra los handlers de recursos estaticos para exponer los assets generados.
     *
     * @param registry registro de handlers de recursos de Spring MVC
     */
    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        // Sirve /assets/** desde classpath:/static/assets/ respetando el context-path (/irpfhelper)
        registry.addResourceHandler("/assets/**")
                .addResourceLocations("classpath:/static/assets/");
    }
}

