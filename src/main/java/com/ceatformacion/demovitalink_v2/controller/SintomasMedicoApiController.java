package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.SintomasDTO;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.SintomasRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API REST para que médicos accedan a síntomas de sus pacientes
 */
@RestController
@RequestMapping("/api/medico/sintomas")
public class SintomasMedicoApiController {

    private final SintomasRepository sintomasRepository;
    private final UsuariosRepository usuariosRepository;

    public SintomasMedicoApiController(SintomasRepository sintomasRepository,
                                       UsuariosRepository usuariosRepository) {
        this.sintomasRepository = sintomasRepository;
        this.usuariosRepository = usuariosRepository;
    }

    /**
     * Obtener síntomas de un paciente específico
     * Endpoint: GET /api/medico/sintomas/{userId}
     *
     * ⚠️ TEMPORAL: Sin restricción de rol para debug
     */
    @Transactional(readOnly = true) // ← AÑADIDO para resolver LazyInitializationException
    @GetMapping("/simple/{userId}")  // Cambiar de "/{userId}" a "/simple/{userId}"
    public ResponseEntity<List<SintomasDTO>> obtenerSintomasPorPaciente(
            @PathVariable("userId") int userId,
            @AuthenticationPrincipal UsuariosDetails auth
    ) {
        try {
            // Buscar el usuario/paciente
            Usuarios paciente = usuariosRepository.findById(userId)
                    .orElse(null);

            if (paciente == null) {
                // Usuario no encontrado - devolver lista vacía o 404
                return ResponseEntity.ok(List.of()); // Lista vacía
            }

            // Obtener síntomas del paciente ordenados por fecha descendente
            List<SintomasDTO> sintomas = sintomasRepository
                    .findByUsuarioOrderByFechaRegistroDesc(paciente)
                    .stream()
                    .map(SintomasDTO::from)
                    .toList();

            return ResponseEntity.ok(sintomas);

        } catch (Exception e) {
            // Log del error para debug
            System.err.println("Error obteniendo síntomas del paciente " + userId + ": " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(List.of());
        }
    }
}