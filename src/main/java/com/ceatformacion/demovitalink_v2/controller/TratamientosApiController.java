package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.services.TratamientoService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tratamientos")
public class TratamientosApiController {

    private final TratamientoService tratamientoService;

    public TratamientosApiController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    // Solo requiere sesión iniciada
    @PreAuthorize("isAuthenticated()") // o elimina la anotación si tu Security ya protege /api/**
    @GetMapping("/mios")
    public List<Tratamientos> misTratamientos(@AuthenticationPrincipal UsuariosDetails auth) {
        return tratamientoService.obtenerTratamientosPorUsuario(auth.getUsuario());
    }
}

