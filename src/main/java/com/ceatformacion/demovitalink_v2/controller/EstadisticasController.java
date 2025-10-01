package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EstadisticasController {
    @GetMapping("/estadisticasUsuario")
    @PreAuthorize("isAuthenticated()")
    public String pagina(Model model, @AuthenticationPrincipal UsuariosDetails user) {
        model.addAttribute("id_usuario", user.getUsuario().getId_usuario());
        return "estadisticasUsuario";
    }
}

