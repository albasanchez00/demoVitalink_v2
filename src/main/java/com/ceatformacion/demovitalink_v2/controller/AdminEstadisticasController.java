package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.admin.*;
import com.ceatformacion.demovitalink_v2.services.AdminEstadisticasService;
import org.springframework.data.domain.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
// Acepta llamadas con /api/admin/stats/... y también con /api/admin/...
@RequestMapping({"/api/admin/stats", "/api/admin"})
// Soporta tanto 'ADMIN' como 'ROLE_ADMIN'
@PreAuthorize("hasAnyAuthority('ADMIN','ROLE_ADMIN')")
public class AdminEstadisticasController {

    private final AdminEstadisticasService statsService;

    public AdminEstadisticasController(AdminEstadisticasService statsService) {
        this.statsService = statsService;
    }

    /* ====================== KPIs ====================== */
    @GetMapping("/overview")
    public OverviewStatsDTO overview(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) Long medicoId
    ) {
        return statsService.overview(from, to, medicoId);
    }

    /* ====================== Series ====================== */
    @GetMapping("/series/adherencia")
    public SerieDTO serieAdherencia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "day") String group
    ) {
        return statsService.serieAdherencia(from, to, group);
    }

    @GetMapping("/series/citas")
    public CitasSerieDTO serieCitas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "day") String group,
            @RequestParam(required = false) Long medicoId
    ) {
        return statsService.serieCitas(from, to, group, medicoId);
    }

    @GetMapping("/series/tratamientos")
    public Map<String,Object> serieTratamientos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "day") String group
    ) {
        return statsService.serieTratamientos(from, to, group);
    }

    @GetMapping("/series/usuarios")
    public Map<String,Object> serieUsuarios(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "day") String group
    ) {
        return statsService.serieUsuarios(from, to, group);
    }

    // Top Síntomas (acepta ambas rutas)
    @GetMapping({"/series/sintomas", "/top/sintomas"})
    public List<TopSintomaDTO> topSintomas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "5") int limit
    ) {
        return statsService.topSintomas(from, to, limit);
    }

    // Distribución por especialidades (acepta ambas rutas)
    @GetMapping({"/series/especialidades", "/distrib/especialidades"})
    public List<DistribEspecialidadDTO> distribEspecialidades(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return statsService.distribEspecialidades(from, to);
    }

    /* ============= Reporte Citas (acepta ambas rutas) =============
       - /api/admin/stats/reportes/citas
       - /api/admin/reportes/citas
     */
    @GetMapping({"/reportes/citas", "/stats/reportes/citas"})
    public Page<Map<String,Object>> reporteCitas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha,desc") String sort,
            @RequestParam(required = false) Long medicoId
    ) {
        return statsService.reporteCitas(from, to, medicoId, page, size, sort);
    }

    // Reporte: Tratamientos
    @GetMapping({"/reportes/tratamientos", "/stats/reportes/tratamientos"})
    public Page<Map<String,Object>> reporteTratamientos(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha_inicio,desc") String sort
    ) {
        return statsService.reporteTratamientos(from, to, page, size, sort);
    }
    // Reporte: Adherencia (proxy)
    @GetMapping({"/reportes/adherencia", "/stats/reportes/adherencia"})
    public Page<Map<String,Object>> reporteAdherencia(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "pct,desc") String sort
    ) {
        return statsService.reporteAdherencia(from, to, page, size, sort);
    }
    // Reporte de Síntomas
    @GetMapping({"/reportes/sintomas"})
    public Page<Map<String, Object>> reporteSintomas(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha_registro,desc") String sort
    ) {
        return statsService.reporteSintomas(from, to, page, size, sort);
    }
}