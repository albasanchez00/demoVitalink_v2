package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.EventoHistorialDTO;
import com.ceatformacion.demovitalink_v2.services.HistorialService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class HistorialApiController {

    private final HistorialService historialService;

    public HistorialApiController(HistorialService historialService) {
        this.historialService = historialService;
    }

    /**
     * Obtiene el historial de un usuario específico.
     *
     * GET /api/usuarios/{id}/historial
     *
     * Accesible por:
     * - El propio usuario (si id coincide con el autenticado)
     * - Médicos (para ver historial de sus pacientes)
     * - Admins
     *
     * @param id                 ID del usuario/paciente
     * @param tipo               Filtro: SINTOMA, TRATAMIENTO, CITA (opcional)
     * @param desde              Fecha inicio (opcional, formato: yyyy-MM-dd)
     * @param hasta              Fecha fin (opcional, formato: yyyy-MM-dd)
     * @param zona               Zona corporal para síntomas (opcional)
     * @param estadoTratamiento  Estado del tratamiento (opcional)
     * @param page               Número de página (default: 0)
     * @param size               Tamaño de página (default: 20)
     */
    @GetMapping("/usuarios/{id}/historial")
    public ResponseEntity<Page<EventoHistorialDTO>> getHistorialUsuario(
            @PathVariable("id") Integer id,
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(value = "zona", required = false) String zona,
            @RequestParam(value = "estadoTratamiento", required = false) String estadoTratamiento,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UsuariosDetails auth
    ) {
        // Validación de seguridad básica
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        // TODO: Añadir validación de permisos según rol
        // - Usuario normal: solo puede ver su propio historial
        // - Médico: puede ver historial de sus pacientes vinculados
        // - Admin: puede ver cualquier historial

        Pageable pageable = PageRequest.of(page, size);
        Page<EventoHistorialDTO> historial = historialService.obtenerHistorial(
                id, tipo, desde, hasta, zona, estadoTratamiento, pageable
        );

        return ResponseEntity.ok(historial);
    }

    /**
     * Endpoint alternativo: historial del usuario autenticado.
     *
     * GET /api/mi-historial
     */
    @GetMapping("/mi-historial")
    public ResponseEntity<Page<EventoHistorialDTO>> getMiHistorial(
            @RequestParam(value = "tipo", required = false) String tipo,
            @RequestParam(value = "desde", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(value = "hasta", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(value = "zona", required = false) String zona,
            @RequestParam(value = "estadoTratamiento", required = false) String estadoTratamiento,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @AuthenticationPrincipal UsuariosDetails auth
    ) {
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        Integer userId = auth.getUsuario().getId_usuario();
        Pageable pageable = PageRequest.of(page, size);

        Page<EventoHistorialDTO> historial = historialService.obtenerHistorial(
                userId, tipo, desde, hasta, zona, estadoTratamiento, pageable
        );

        return ResponseEntity.ok(historial);
    }

    /**
     * Estadísticas del historial de un usuario.
     *
     * GET /api/usuarios/{id}/historial/stats
     */
    @GetMapping("/usuarios/{id}/historial/stats")
    public ResponseEntity<Map<String, Object>> getHistorialStats(
            @PathVariable("id") Integer id,
            @AuthenticationPrincipal UsuariosDetails auth
    ) {
        if (auth == null) {
            return ResponseEntity.status(401).build();
        }

        HistorialService.HistorialStats stats = historialService.obtenerEstadisticas(id);

        Map<String, Object> response = new HashMap<>();
        response.put("totalSintomas", stats.getTotalSintomas());
        response.put("totalTratamientos", stats.getTotalTratamientos());
        response.put("totalCitas", stats.getTotalCitas());
        response.put("total", stats.getTotal());

        return ResponseEntity.ok(response);
    }
}