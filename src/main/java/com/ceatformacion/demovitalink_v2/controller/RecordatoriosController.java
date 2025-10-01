package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RecordatoriosController {
    @GetMapping("/usuarios/recordatorios") // 👈 ruta distinta
    public String recordatorios(Model model, @AuthenticationPrincipal UsuariosDetails auth) {
        Integer idUsuario = auth.getUsuario().getId_usuario();
        model.addAttribute("id_usuario", idUsuario);
        return "recordatorios";
    }
}