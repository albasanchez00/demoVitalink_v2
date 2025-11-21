package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/docs")
public class DocumentacionController {

    // Página principal de documentación - layout.html
    @GetMapping("")
    public String documentacionPrincipal(Model model) {
        model.addAttribute("seccion", "inicio");
        model.addAttribute("titulo", "Documentación - VitaLink");
        return "documentacion/layout";
    }

    // Alias para /docs/inicio
    @GetMapping("/inicio")
    public String documentacionInicio(Model model) {
        model.addAttribute("seccion", "inicio");
        model.addAttribute("titulo", "Documentación - VitaLink");
        return "documentacion/layout";
    }

    // Guía de usuario - guiaUsuario.html
    @GetMapping("/usuario")
    public String documentacionUsuario(Model model) {
        model.addAttribute("seccion", "usuario");
        model.addAttribute("titulo", "Guía de Usuario - VitaLink");
        return "documentacion/guiaUsuario";
    }

    // Guía para médicos - guiaMedico.html
    @GetMapping("/medico")
    public String documentacionMedico(Model model) {
        model.addAttribute("seccion", "medico");
        model.addAttribute("titulo", "Guía para Médicos - VitaLink");
        return "documentacion/guiaMedico";
    }

    // Documentación de API - api.html
    @GetMapping("/api")
    public String documentacionApi(Model model) {
        model.addAttribute("seccion", "api");
        model.addAttribute("titulo", "API Reference - VitaLink");
        return "documentacion/api";
    }

    // FAQ - redirige a layout con anclaje
    @GetMapping("/faq")
    public String documentacionFaq(Model model) {
        model.addAttribute("seccion", "faq");
        model.addAttribute("titulo", "Preguntas Frecuentes - VitaLink");
        return "documentacion/layout";
    }
}