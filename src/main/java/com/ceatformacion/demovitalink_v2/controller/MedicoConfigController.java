package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/medico")
@PreAuthorize("hasRole('MEDICO')")
public class MedicoConfigController {
    @GetMapping("/configMedico")
    public String vistaConfigMedico() {
        // Como el archivo está en src/main/resources/templates/configMedico.html
        return "configMedico";
    }

    // (Opcional) mapea alias más “bonito”
    @GetMapping("/configuracion")
    public String aliasConfiguracion() {
        return "configMedico";
    }
}
