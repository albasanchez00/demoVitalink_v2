package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.PanelUsuarioVM;
import com.ceatformacion.demovitalink_v2.services.PanelUsuarioService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/panel")
public class PanelUsuarioApiController {

    private final PanelUsuarioService panelUsuarioService;

    public PanelUsuarioApiController(PanelUsuarioService panelUsuarioService) {
        this.panelUsuarioService = panelUsuarioService;
    }

    @GetMapping("/overview")
    public PanelUsuarioVM overview(Authentication auth) {
        String username = (auth != null ? auth.getName() : null);
        return panelUsuarioService.cargarPanel(username);
    }
}
