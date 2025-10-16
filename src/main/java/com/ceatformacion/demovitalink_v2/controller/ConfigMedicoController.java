package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.config.ConfigMedicoDTO;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.services.ConfigMedicoService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medico/config")
@PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
public class ConfigMedicoController {

    private final ConfigMedicoService service;

    public ConfigMedicoController(ConfigMedicoService service) {
        this.service = service;
    }

    @GetMapping
    public ConfigMedicoDTO get(@AuthenticationPrincipal UsuariosDetails user) {
        Usuarios medico = user.getUsuario();
        return service.getOrCreate(medico);
    }

    @PutMapping
    public ConfigMedicoDTO put(@AuthenticationPrincipal UsuariosDetails user,
                               @Valid @RequestBody ConfigMedicoDTO dto) {
        Usuarios medico = user.getUsuario();
        return service.saveAll(medico, dto);
    }
}
