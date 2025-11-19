package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.services.TratamientoService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tratamientos")
public class TratamientosApiController {

    private final TratamientoService tratamientoService;

    public TratamientosApiController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    /**
     * Obtener tratamientos del usuario autenticado
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/mios")
    public List<Map<String, Object>> misTratamientos(@AuthenticationPrincipal UsuariosDetails auth) {
        List<Tratamientos> tratamientos = tratamientoService.obtenerTratamientosPorUsuario(auth.getUsuario());
        return mapearTratamientos(tratamientos);
    }

    /**
     * Obtener tratamientos de un usuario específico (para médicos)
     * Endpoint: GET /api/tratamientos?userId={id}
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping
    public List<Map<String, Object>> obtenerTratamientos(
            @RequestParam(value = "userId", required = false) Integer userId,
            @AuthenticationPrincipal UsuariosDetails auth
    ) {
        // Si viene userId y el usuario es médico, devolver tratamientos de ese paciente
        if (userId != null) {
            // Verificar que el usuario autenticado es médico o admin
            String rol = auth.getUsuario().getRol().name();
            if (rol.equals("MEDICO") || rol.equals("ADMIN")) {
                List<Tratamientos> tratamientos = tratamientoService.obtenerTratamientosPorIdUsuario(userId);
                return mapearTratamientos(tratamientos);
            }
        }

        // Si no viene userId o no es médico, devolver tratamientos del usuario autenticado
        List<Tratamientos> tratamientos = tratamientoService.obtenerTratamientosPorUsuario(auth.getUsuario());
        return mapearTratamientos(tratamientos);
    }

    /**
     * Mapea los tratamientos a un formato JSON amigable para el historial
     */
    private List<Map<String, Object>> mapearTratamientos(List<Tratamientos> tratamientos) {
        return tratamientos.stream().map(t -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", t.getId_tratamiento());
            map.put("nombre", t.getNombre_tratamiento());
            map.put("titulo", t.getNombre_tratamiento()); // Alias para compatibilidad
            map.put("dosis", t.getDosis());
            map.put("frecuencia", t.getFrecuencia());
            map.put("formula", t.getFormula());
            map.put("observaciones", t.getObservaciones());
            map.put("estado", t.getEstado_tratamiento());
            map.put("fechaInicio", t.getFecha_inicio() != null ? t.getFecha_inicio().toString() : null);
            map.put("fechaFin", t.getFecha_fin() != null ? t.getFecha_fin().toString() : null);
            map.put("tomaAlimentos", t.isToma_alimentos());

            // Para el historial, usar fechaInicio como fecha principal
            if (t.getFecha_inicio() != null) {
                map.put("createdAt", t.getFecha_inicio().toString());
            }

            return map;
        }).collect(Collectors.toList());
    }
}

