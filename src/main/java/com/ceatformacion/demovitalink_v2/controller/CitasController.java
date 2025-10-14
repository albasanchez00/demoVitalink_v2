package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.CitasService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CitasController {

    private final CitasService citasService;
    private final UsuariosRepository usuariosRepository;

    public CitasController(CitasService citasService, UsuariosRepository usuariosRepository) {
        this.citasService = citasService;
        this.usuariosRepository = usuariosRepository;
    }

    // FORMULARIO + LISTA DE MIS CITAS
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/pedirCita")
    public String mostrarFormularioCita(@AuthenticationPrincipal UsuariosDetails principal, Model model) {
        Usuarios usuario = usuariosRepository.findById(principal.getUsuario().getId_usuario())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        model.addAttribute("citas", new Citas()); // backing bean del form
        model.addAttribute("misCitas", citasService.obtenerCitasPorUsuario(usuario)); // lista para la vista

        return "pedirCita";
    }

    // GUARDAR CITA
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/guardarCitas")
    public String guardarCita(@AuthenticationPrincipal UsuariosDetails principal,
                              @ModelAttribute("citas") Citas cita,
                              RedirectAttributes ra) {
        Usuarios usuario = usuariosRepository.findById(principal.getUsuario().getId_usuario())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        // Asigna el paciente (usuario) a la cita
        cita.setUsuario(usuario);

        // Si en tu entidad Citas tienes estado/duración por defecto, no hace falta tocarlos aquí
        citasService.guardarCita(cita);

        ra.addFlashAttribute("ok", "Cita creada correctamente.");
        return "redirect:/pedirCita";
    }

    // (OPCIONAL) ENDPOINT JSON PARA CALENDARIO DEL USUARIO LOGUEADO
    @PreAuthorize("hasRole('USER')")
    @GetMapping(value = "/api/citas/mias", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> citasMias(@AuthenticationPrincipal UsuariosDetails principal) {
        Usuarios usuario = usuariosRepository.findById(principal.getUsuario().getId_usuario())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        return citasService.obtenerCitasPorUsuario(usuario).stream()
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId_cita());
                    // usa título si existe, sino "Cita"
                    m.put("title", (c.getTitulo() != null && !c.getTitulo().isBlank())
                            ? c.getTitulo()
                            : "Cita");
                    LocalDateTime start = LocalDateTime.of(c.getFecha(), c.getHora());
                    LocalDateTime end = start.plusMinutes(c.getDuracionMinutos());
                    m.put("start", start.toString());
                    m.put("end", end.toString());
                    m.put("estado", c.getEstado().name());
                    return m;
                })
                .toList();
    }
}