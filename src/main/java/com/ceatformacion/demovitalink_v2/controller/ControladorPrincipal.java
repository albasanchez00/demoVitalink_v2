package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

    // Solicitar demo
    @GetMapping("/demo")
    public String demo(Model model) {
        model.addAttribute("titulo", "Solicitar Demo - VitaLink");
        return "demo";
    }

    // Procesar solicitud de demo
    @PostMapping("/demo")
    public String procesarDemo(@RequestParam String nombre,
                               @RequestParam String empresa,
                               @RequestParam String email,
                               @RequestParam String telefono,
                               @RequestParam String mensaje,
                               Model model) {
        // Aquí procesarías la solicitud (enviar email, guardar en BD, etc.)
        model.addAttribute("mensaje", "Gracias por tu interés. Te contactaremos pronto.");
        return "demo-confirmacion";
    }
}
