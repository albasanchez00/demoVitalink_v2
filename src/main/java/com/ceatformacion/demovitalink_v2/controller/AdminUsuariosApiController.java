package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.UsuarioLiteDTO;
import com.ceatformacion.demovitalink_v2.services.UsuariosService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUsuariosApiController {

    private final UsuariosService service;

    public AdminUsuariosApiController(UsuariosService service) {
        this.service = service;
    }

    @GetMapping
    public Page<UsuarioLiteDTO> buscar(
            @RequestParam(value = "q", required = false) String q,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return service.buscarLigero(q, page, size);
    }
}