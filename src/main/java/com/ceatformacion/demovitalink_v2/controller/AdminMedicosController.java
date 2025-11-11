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

import java.util.Map;

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
    // ✅ POST: crear médico (lo que te falta → causa del 405)
    @PostMapping
    public ResponseEntity<Integer> crear(
            @RequestParam String username,
            @RequestParam String password
    ) {
        Integer id = service.crearMedico(username, password);
        return ResponseEntity.ok(id); // JSON válido: 123
    }
    // (opcional) DELETE si lo usas desde la tabla
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Integer id) {
        service.eliminarMedico(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<?> actualizarMedico(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> datos
    ) {
        try {
            service.actualizarMedico(id, datos);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.internalServerError().body(ex.getMessage());
        }
    }

}