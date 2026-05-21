package com.irpf.backend.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controlador de apoyo para aplicaciones SPA.
 * Reenvia rutas del frontend a index.html para que Angular resuelva la navegacion.
 */
@Controller
public class SpaForwardController {

    /**
     * Reenvia rutas de navegacion del cliente al punto de entrada de la SPA.
     *
     * @return recurso de destino a servir por Spring MVC
     */
    @GetMapping({
            "/login",
            "/crear-usuario",
            "/usuarios/crear",
            "/verificar-email",
            "/principal",
            "/personas",
            "/usuarios",
            "/datos-personales",
            "/datos-economicos",
            "/prevision-ingresos",
            "/puestos-tipo",
            "/contratos",
            "/simulacion",
            "/resultados",
            "/cambiar-password"
    })
    public String forwardSpaRoutes() {
        return "forward:/index.html";
    }
}
