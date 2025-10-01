package com.ceatformacion.demovitalink_v2.controller;


import com.ceatformacion.demovitalink_v2.dto.RecordatoriosDTO;
import com.ceatformacion.demovitalink_v2.model.TipoRecordatorio;
import com.ceatformacion.demovitalink_v2.services.RecordatoriosService;
import com.ceatformacion.demovitalink_v2.services.VinculosService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/recordatorios")
public class RecordatoriosApiController {

    private final RecordatoriosService service;
    private final VinculosService vinculosService;

    public RecordatoriosApiController(RecordatoriosService service, VinculosService vinculosService) {
        this.service = service;
        this.vinculosService = vinculosService;
    }

    @PreAuthorize("@sec.isOwner(#idUsuario, authentication)")
    @GetMapping
    public List<RecordatoriosDTO> listar(
            @RequestParam("id_usuario") int idUsuario,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime hasta,
            @RequestParam(required = false) TipoRecordatorio tipo
    ) {
        if (tipo != null) return service.listarPorTipo(idUsuario, tipo);
        if (desde != null && hasta != null) return service.listarRango(idUsuario, desde, hasta);
        return service.listar(idUsuario);
    }

    @PostMapping
    public RecordatoriosDTO crear(@Valid @RequestBody RecordatoriosDTO dto) {
        return service.crear(dto);
    }

    @PutMapping("/{id}")
    public RecordatoriosDTO actualizar(@PathVariable("id") int id_recordatorio,
                                       @Valid @RequestBody RecordatoriosDTO dto) {
        return service.actualizar(id_recordatorio, dto);
    }

    @PatchMapping("/{id}/completado")
    public RecordatoriosDTO toggle(@PathVariable("id") int id_recordatorio,
                                   @RequestParam boolean value) {
        return service.toggleCompletado(id_recordatorio, value);
    }

    @DeleteMapping("/{id}")
    public void eliminar(@PathVariable("id") int id_recordatorio) {
        service.eliminar(id_recordatorio);
    }

    @PreAuthorize("@sec.isOwner(#idUsuario, authentication)")
    @GetMapping("/vinculos-activos")
    public Map<String, Object> vinculosActivos(@RequestParam("id_usuario") int idUsuario) {
        return vinculosService.obtenerVinculosActivos(idUsuario);
    }
}