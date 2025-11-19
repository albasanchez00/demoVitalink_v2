package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.FiltroHistorial;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HistorialVistaController {

    /**
     * Historial del paciente (puede ser el usuario logueado o un paciente específico)
     *
     * @param auth Usuario autenticado
     * @param userId ID del usuario/paciente a consultar (opcional, si no se pasa usa el usuario autenticado)
     * @param filtro Filtros de búsqueda
     * @param model Modelo Spring
     */
    @GetMapping("/usuarios/historialPaciente")
    public String historialPaciente(
            @AuthenticationPrincipal UsuariosDetails auth,
            @RequestParam(value = "userId", required = false) Integer userId,
            @ModelAttribute(value = "filtro") FiltroHistorial filtro,
            Model model
    ) {
        if (filtro == null) {
            filtro = new FiltroHistorial();
            filtro.setMostrarAvanzados(false);
        }
        model.addAttribute("filtro", filtro);

        // Si viene userId en la URL, usar ese; si no, usar el usuario autenticado
        if (userId != null) {
            model.addAttribute("userId", userId);
        } else if (auth != null) {
            model.addAttribute("userId", auth.getUsuario().getId_usuario());
        }

        return "historialPaciente";
    }

    /**
     * Alias opcional: /historialPaciente
     */
    @GetMapping("/historialPaciente")
    public String historialPacienteAlias(
            @AuthenticationPrincipal UsuariosDetails auth,
            @RequestParam(value = "userId", required = false) Integer userId,
            @ModelAttribute(value = "filtro") FiltroHistorial filtro,
            Model model
    ) {
        if (filtro == null) {
            filtro = new FiltroHistorial();
            filtro.setMostrarAvanzados(false);
        }
        model.addAttribute("filtro", filtro);

        // Si viene userId en la URL, usar ese; si no, usar el usuario autenticado
        if (userId != null) {
            model.addAttribute("userId", userId);
        } else if (auth != null) {
            model.addAttribute("userId", auth.getUsuario().getId_usuario());
        }

        return "historialPaciente";
    }
}
