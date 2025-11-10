package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.config.ConfigMedicoDTO;
import com.ceatformacion.demovitalink_v2.dto.config.NotificacionesDTO;
import com.ceatformacion.demovitalink_v2.model.ConfigNotificaciones;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.services.ConfigMedicoService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    public static void merge(ConfigNotificaciones entity, NotificacionesDTO dto) {
        if (entity == null || dto == null) return;
        try {
            ObjectMapper mapper = new ObjectMapper();

            // Convierte los DTOs a JSON string para guardar
            if (dto.canales() != null)
                entity.setCanalesJson(mapper.writeValueAsString(dto.canales()));

            if (dto.eventos() != null)
                entity.setEventosJson(mapper.writeValueAsString(dto.eventos()));

            if (dto.plantillas() != null)
                entity.setPlantillasJson(mapper.writeValueAsString(dto.plantillas()));

            entity.setSilencioDesde(dto.silencioDesde());
            entity.setSilencioHasta(dto.silencioHasta());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
