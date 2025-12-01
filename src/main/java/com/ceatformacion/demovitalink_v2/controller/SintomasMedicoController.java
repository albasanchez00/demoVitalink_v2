package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.SintomasDTO;
import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.model.TipoSintoma;
import com.ceatformacion.demovitalink_v2.model.ZonaCorporal;
import com.ceatformacion.demovitalink_v2.services.SintomasService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@PreAuthorize("hasRole('MEDICO')")
@RestController
@RequestMapping("/api/medico/sintomas")
public class SintomasMedicoController {
    private final SintomasService service;

    public SintomasMedicoController(SintomasService service) {
        this.service = service;
    }
    @GetMapping
    public ResponseEntity<String> faltaIdUsuario() {
        return ResponseEntity.badRequest().body("Falta el idUsuario en la ruta: /api/medico/sintomas/{idUsuario}");
    }
    // Listado del paciente con filtros (ENUM) + paginación
    @GetMapping("/{idUsuario}")
    public Page<SintomasDTO> listarPorUsuario(
            @PathVariable Integer idUsuario,
            @RequestParam(required = false) TipoSintoma tipo,          // <-- Enum
            @RequestParam(required = false) ZonaCorporal zona,         // <-- Enum
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaRegistro,desc") String sort
    ) {
        if (desde != null && hasta != null && hasta.isBefore(desde)) {
            return Page.empty(); // o lanzar 400 con un ApiError si prefieres
        }

        // Parseo robusto del sort (campo,dirección)
        String[] parts = sort.split(",");
        Sort sortBy = (parts.length == 2)
                ? Sort.by(Sort.Order.by(parts[0]).with(Sort.Direction.fromString(parts[1])))
                : Sort.by(sort).descending();

        Pageable pageable = PageRequest.of(page, size, sortBy);

        return service
                .listarPaginadoConFiltros(idUsuario, tipo, zona, desde, hasta, pageable)
                .map(SintomasDTO::from);
    }

    // Crear síntoma para un paciente concreto (el body ya trae enums)
    @PostMapping("/{idUsuario}")
    public ResponseEntity<SintomasDTO> crear(
            @PathVariable Integer idUsuario,
            @RequestBody Sintomas body
    ) {
        // Coherencia: el body debe apuntar al mismo usuario del path
        Integer bodyUserId = (body.getUsuario() != null ? body.getUsuario().getId_usuario() : null);
        if (bodyUserId == null || !bodyUserId.equals(idUsuario)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        if (body.getFechaRegistro() == null) {
            body.setFechaRegistro(LocalDateTime.now());
        }

        Sintomas creado = service.crear(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(SintomasDTO.from(creado));
    }

    // Detalle por id
    @GetMapping("/detalle/{idSintoma}")
    public ResponseEntity<SintomasDTO> detalle(@PathVariable int idSintoma) {
        return service.obtenerPorId(idSintoma)
                .map(SintomasDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Actualizar síntoma existente
    @PutMapping("/detalle/{idSintoma}")
    public ResponseEntity<SintomasDTO> actualizar(
            @PathVariable int idSintoma,
            @RequestBody Sintomas body
    ) {
        return service.actualizar(idSintoma, body)
                .map(SintomasDTO::from)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // Eliminar por id
    @DeleteMapping("/detalle/{idSintoma}")
    public ResponseEntity<Void> eliminar(@PathVariable int idSintoma) {
        service.eliminar(idSintoma);
        return ResponseEntity.noContent().build();
    }
}