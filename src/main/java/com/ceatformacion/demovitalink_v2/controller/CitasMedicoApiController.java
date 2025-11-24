package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.CitasService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * API REST para que médicos accedan a citas de sus pacientes
 */
@RestController
@RequestMapping("/api/medico/citas")
public class CitasMedicoApiController {

    private final CitasService citasService;
    private final UsuariosRepository usuariosRepository;

    public CitasMedicoApiController(CitasService citasService, UsuariosRepository usuariosRepository) {
        this.citasService = citasService;
        this.usuariosRepository = usuariosRepository;
    }

    /**
     * Obtener citas de un paciente específico
     * Endpoint: GET /api/medico/citas/{userId}
     *
     * ⚠️ TEMPORAL: Sin restricción de rol para debug
     */
    @Transactional(readOnly = true)
    @GetMapping("/{userId}")
    public ResponseEntity<List<Map<String, Object>>> obtenerCitasPorPaciente(
            @PathVariable("userId") int userId,
            @AuthenticationPrincipal UsuariosDetails auth
    ) {
        try {
            // Buscar el usuario/paciente
            Usuarios paciente = usuariosRepository.findById(userId)
                    .orElse(null);

            if (paciente == null) {
                // Usuario no encontrado - devolver lista vacía
                return ResponseEntity.ok(List.of());
            }

            // Obtener citas del paciente
            List<Citas> citas = citasService.obtenerCitasPorUsuario(paciente);

            // Mapear a formato compatible con historial.js
            List<Map<String, Object>> citasDTO = citas.stream()
                    .map(this::mapearCita)
                    .toList();

            return ResponseEntity.ok(citasDTO);

        } catch (Exception e) {
            // Log del error para debug
            System.err.println("Error obteniendo citas del paciente " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }
    }

    /**
     * Mapea una Cita a un Map compatible con el historial.js
     */
    private Map<String, Object> mapearCita(Citas cita) {
        Map<String, Object> map = new HashMap<>();

        map.put("id", cita.getId_cita());
        map.put("titulo", cita.getTitulo() != null ? cita.getTitulo() : "Cita");
        map.put("descripcion", cita.getDescripcion());
        map.put("fecha", cita.getFecha() != null ? cita.getFecha().toString() : null);
        map.put("hora", cita.getHora() != null ? cita.getHora().toString() : null);

        // Para compatibilidad con el mapper de historial.js
        if (cita.getFecha() != null && cita.getHora() != null) {
            LocalDateTime fechaHora = LocalDateTime.of(cita.getFecha(), cita.getHora());
            map.put("fechaHora", fechaHora.toString());
            map.put("fechaCita", cita.getFecha().toString());
            map.put("horaCita", cita.getHora().toString());
        }

        // Info del médico si existe
        if (cita.getMedico() != null) {
            String nombreMedico = cita.getMedico().getCliente() != null
                    ? cita.getMedico().getCliente().getNombre() + " " + cita.getMedico().getCliente().getApellidos()
                    : cita.getMedico().getUsername();
            map.put("especialista", nombreMedico);
        }

        map.put("estado", cita.getEstado() != null ? cita.getEstado().name() : null);
        map.put("duracionMinutos", cita.getDuracionMinutos());

        return map;
    }
}