package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/medico")
public class PacientesVistaController {
    @GetMapping("/pacientes")
    public String pacientesVinculados(Model model) {
        model.addAttribute("page", "pacientes");
        return "pacientesMedico";
    }
}
