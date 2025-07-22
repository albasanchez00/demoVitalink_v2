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

    @GetMapping("/terminoCondiciones")
    public String terminosCondiciones() {
        return "terminoCondiciones";
    }
}
