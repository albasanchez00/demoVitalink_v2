package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.services.CitasService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/citas")
public class CitasRestController {
    @Autowired
    private CitasService citasService;

    @GetMapping("/pedirCita")
    public String mostrarFormularioCita() {
        return "pedirCita"; // sin la extensión .html
    }

    /**
     * Obtener citas del usuario autenticado
     */
    @GetMapping
    public List<Map<String, Object>> obtenerCitasDelUsuarioAutenticado(
            @RequestParam(value = "userId", required = false) Integer userId
    ) {
        // Si viene userId y el usuario es médico, devolver las citas de ese paciente
        if (userId != null) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            UsuariosDetails userDetails = (UsuariosDetails) auth.getPrincipal();

            // Solo permitir a médicos ver citas de otros usuarios
            if (userDetails.getUsuario().getRol().name().equals("MEDICO") ||
                    userDetails.getUsuario().getRol().name().equals("ADMIN")) {

                Usuarios paciente = new Usuarios();
                paciente.setId_usuario(userId);

                List<Citas> citas = citasService.obtenerCitasPorUsuario(paciente);
                return mapearCitas(citas);
            }
        }

        // Si no viene userId o no es médico, devolver las citas del usuario autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UsuariosDetails userDetails = (UsuariosDetails) auth.getPrincipal();
        Usuarios usuario = userDetails.getUsuario();

        List<Citas> citas = citasService.obtenerCitasPorUsuario(usuario);
        return mapearCitas(citas);
    }

    /**
     * Mapea las citas a un formato JSON amigable
     */
    private List<Map<String, Object>> mapearCitas(List<Citas> citas) {
        return citas.stream().map(cita -> {
            Map<String, Object> evento = new HashMap<>();
            evento.put("id", cita.getId_cita());
            evento.put("titulo", cita.getTitulo());
            evento.put("descripcion", cita.getDescripcion());
            evento.put("fecha", cita.getFecha() != null ? cita.getFecha().toString() : null);
            evento.put("hora", cita.getHora() != null ? cita.getHora().toString() : null);

            // Para compatibilidad con el formato anterior
            if (cita.getFecha() != null && cita.getHora() != null) {
                String start = LocalDateTime.of(cita.getFecha(), cita.getHora()).toString();
                String end = LocalDateTime.of(cita.getFecha(), cita.getHora().plusHours(1)).toString();
                evento.put("start", start);
                evento.put("end", end);
                evento.put("fechaHora", start); // Para el mapper del historial.js
            }

            // Info del médico si existe
            if (cita.getMedico() != null) {
                evento.put("especialista", cita.getMedico().getCliente() != null
                        ? cita.getMedico().getCliente().getNombre() + " " + cita.getMedico().getCliente().getApellidos()
                        : cita.getMedico().getUsername());
            }

            evento.put("estado", cita.getEstado() != null ? cita.getEstado().name() : null);

            return evento;
        }).collect(Collectors.toList());
    }
}