package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.ConfigAdminDTO;
import com.ceatformacion.demovitalink_v2.services.ConfigAdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigAdminController {
    private final ConfigAdminService service;

    public ConfigAdminController(ConfigAdminService service) { this.service = service; }

    @GetMapping
    public ResponseEntity<ConfigAdminDTO> get() {
        return ResponseEntity.ok(service.getActual());
    }

    @PutMapping
    public ResponseEntity<ConfigAdminDTO> put(@RequestBody ConfigAdminDTO dto) {
        return ResponseEntity.ok(service.save(dto));
    }
}
