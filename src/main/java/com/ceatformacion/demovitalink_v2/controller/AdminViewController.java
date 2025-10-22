package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminViewController {

    @GetMapping("/admin/usuarios")
    public String usuariosAdmin() {
        // -> templates/admin/usuariosAdmin.html
        return "usuariosAdmin";
    }
    @GetMapping("/admin/tratamientos")
    public String pagina(){ return "tratamientosAdmin"; }

    @GetMapping("/admin/citas")
    public String paginaCita(){ return "citasAdmin"; }

    @GetMapping("/admin/estadisticas")
    public String paginaReportes(){ return "reportesAdmin"; }

    @GetMapping("/admin/chat")
    public String paginaMensajesA(){ return "chatAdmin"; }

    @GetMapping("/admin/config")
    public String paginaConfigAdmin(){ return "configAdmin"; }
}