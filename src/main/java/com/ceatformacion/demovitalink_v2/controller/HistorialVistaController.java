package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.FiltroHistorial;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HistorialVistaController {

    @GetMapping("/usuarios/historialPaciente")
    public String historialPaciente(
            @AuthenticationPrincipal UsuariosDetails auth,
            @ModelAttribute(value = "filtro") FiltroHistorial filtro,
            Model model
    ) {
        if (filtro == null) {
            filtro = new FiltroHistorial();
            filtro.setMostrarAvanzados(false);
        }
        model.addAttribute("filtro", filtro);

        if (auth != null) {
            model.addAttribute("userId", auth.getUsuario().getId_usuario());
        }
        return "historialPaciente";
    }

    // Alias opcional: /historialPaciente
    @GetMapping("/historialPaciente")
    public String historialPacienteAlias(
            @AuthenticationPrincipal UsuariosDetails auth,
            @ModelAttribute(value = "filtro") FiltroHistorial filtro,
            Model model
    ) {
        if (filtro == null) {
            filtro = new FiltroHistorial();
            filtro.setMostrarAvanzados(false);
        }
        model.addAttribute("filtro", filtro);

        if (auth != null) {
            model.addAttribute("userId", auth.getUsuario().getId_usuario());
        }
        return "historialPaciente";
    }
}
