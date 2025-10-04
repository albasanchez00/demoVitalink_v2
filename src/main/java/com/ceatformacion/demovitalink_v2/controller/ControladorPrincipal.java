package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class ControladorPrincipal {


    @GetMapping("/")
    public String principal() {
        return "index";
    }

    @GetMapping("/serviciosCliente")
    public String serviciosCliente() {
        return "serviciosCliente";
    }

    @GetMapping("/serviciosEmpresa")
    public String serviciosEmpresa() {
        return "serviciosEmpresa";
    }

    @GetMapping("/contacto")
    public String contactoVitalink() {
        return "contacto";
    }

    @GetMapping({"/politicaPrivacidad"})
    public String politicaPrivacidad() { return "politicaPrivacidad"; }

    @GetMapping({"/terminoCondiciones"})
    public String terminoCondiciones() { return "terminoCondiciones"; }

    @GetMapping({"/politicaCookies"})
    public String politicaCookies() { return "politicaCookies"; }

    @GetMapping({"/baseLegal"})
    public String baseLegal() { return "baseLegal"; }
}
