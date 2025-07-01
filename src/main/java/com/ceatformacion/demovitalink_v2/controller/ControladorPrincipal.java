package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.web.bind.annotation.GetMapping;

public class ControladorPrincipal {
    @GetMapping("/inicioSesion")
    public String login(){return "inicioSesion";}

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



    @GetMapping("/mensajesUsuario")
    public String mensajesUsuario() {
        return "mensajesUsuario";
    }

    @GetMapping("/configUsuario")
    public String configuracionUsuario() {
        return "configUsuario";
    }

    @GetMapping("/pedirCita")
    public String pedirCita() {
        return "pedirCita";
    }

    @GetMapping("/recordatorios")
    public String recordatoriosUsuario() {
        return "recordatorios";
    }

    @GetMapping("/registroSintomas")
    public String registroSintomas() {
        return "registroSintomas";
    }

    @GetMapping("/registroTratamiento")
    public String registroTratamientos() {
        return "registroTratamiento";
    }

}
