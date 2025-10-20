package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.MedicoListDTO;
import com.ceatformacion.demovitalink_v2.services.AdminMedicosService;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/medicos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminMedicosController {

    private final AdminMedicosService service;

    public AdminMedicosController(AdminMedicosService service) {
        this.service = service;
    }

    // AdminMedicosController.java
    @GetMapping
    public ResponseEntity<Page<MedicoListDTO>> listar(
            @RequestParam(required=false) String q,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size,
            @RequestParam(required=false) String sortIgnored // lo ignoramos a propósito
    ) {
        // No permitimos sort entrante (evitamos 'id'); el repo ya ordena por id_usuario
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listarMedicosDTO(q, pageable));
    }


    private Sort safeSort(String sort) {
        // Solo aceptamos propiedades REALES: id_usuario | username
        String prop = "id_usuario";
        boolean asc = false;

        if (sort != null && !sort.isBlank()) {
            String[] p = sort.split(",");
            String candidate = p[0].trim();
            if ("id_usuario".equals(candidate) || "username".equals(candidate)) prop = candidate;
            if (p.length > 1 && "asc".equalsIgnoreCase(p[1])) asc = true;
        }

        try {
            return asc ? Sort.by(prop).ascending() : Sort.by(prop).descending();
        } catch (IllegalArgumentException ex) {
            // Fallback final si algo raro pasa
            return Sort.by("username").ascending();
        }
    }
}