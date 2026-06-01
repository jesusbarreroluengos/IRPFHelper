package com.irpf.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Punto de entrada de la aplicacion backend IRPF Helper.
 * <p>
 * Permite el arranque embebido de Spring Boot y el despliegue como WAR
 * en contenedor externo al extender {@link SpringBootServletInitializer}.
 */
@SpringBootApplication
@EnableScheduling
public class BackendApplication extends SpringBootServletInitializer  {

    /**
     * Configura las fuentes de Spring cuando la aplicacion se despliega como WAR.
     *
     * @param application builder de Spring Boot proporcionado por el contenedor
     * @return builder con la clase principal registrada como origen
     */
	  @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(BackendApplication.class);
    }

	/**
	 * Arranca la aplicacion Spring Boot en modo standalone.
	 *
	 * @param args argumentos de linea de comandos de inicio
	 */
	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
