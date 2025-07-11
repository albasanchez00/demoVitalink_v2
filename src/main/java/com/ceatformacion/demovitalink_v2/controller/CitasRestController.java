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

    @GetMapping
    public List<Map<String, Object>> obtenerCitasDelUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UsuariosDetails userDetails = (UsuariosDetails) auth.getPrincipal();
        Usuarios usuario = userDetails.getUsuario();

        List<Citas> citas = citasService.obtenerCitasPorUsuario(usuario);

        return citas.stream().map(cita -> {
            Map<String, Object> evento = new HashMap<>();
            evento.put("id", cita.getId_cita());
            evento.put("title", cita.getTitulo());
            evento.put("description", cita.getDescripcion());

            String start = LocalDateTime.of(cita.getFecha(), cita.getHora()).toString();
            String end = LocalDateTime.of(cita.getFecha(), cita.getHora().plusHours(1)).toString();
            evento.put("start", start);
            evento.put("end", end);

            return evento;
        }).collect(Collectors.toList());
    }
}
