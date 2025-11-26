package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.EventoCalendarDTO;
import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.CitasService;
import com.ceatformacion.demovitalink_v2.services.DisponibilidadService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CitasController {

    private final CitasService citasService;
    private final UsuariosRepository usuariosRepository;
    private final DisponibilidadService disponibilidadService;

    public CitasController(CitasService citasService,
                           UsuariosRepository usuariosRepository,
                           DisponibilidadService disponibilidadService) {
        this.citasService = citasService;
        this.usuariosRepository = usuariosRepository;
        this.disponibilidadService = disponibilidadService;
    }

    // FORMULARIO + LISTA DE MIS CITAS
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/pedirCita")
    public String mostrarFormularioCita(@AuthenticationPrincipal UsuariosDetails principal, Model model) {
        Usuarios usuario = usuariosRepository.findById(principal.getUsuario().getId_usuario())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        Citas form = new Citas();
        // Evita null en nested path del select (medico.id_usuario)
        form.setMedico(new Usuarios());
        form.getMedico().setId_usuario(0);

        // NOTA: las horas se cargarán por AJAX desde /api/agenda/horas-disponibles
        model.addAttribute("citas", form);
        model.addAttribute("misCitas", citasService.obtenerCitasPorUsuario(usuario));
        model.addAttribute("medicos", usuariosRepository.findByRol(Rol.MEDICO));
        return "pedirCita";
    }

    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    @GetMapping(value = "/api/agenda/horas-disponibles", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<List<String>> horasDisponibles(
            @RequestParam int idMedico,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
            @RequestParam(required = false) Integer slot
    ) {
        if (usuariosRepository.findById(idMedico).isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(disponibilidadService.calcularHorasDisponibles(idMedico, fecha, slot));
    }

    // GUARDAR CITA (con validaciones de disponibilidad)
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/guardarCitas")
    public String guardarCita(@AuthenticationPrincipal UsuariosDetails principal,
                              @ModelAttribute("citas") Citas cita,
                              RedirectAttributes ra) {
        Usuarios usuario = usuariosRepository.findById(principal.getUsuario().getId_usuario())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        // Asignar paciente
        cita.setUsuario(usuario);

        // ===== Validaciones mínimas =====
        if (cita.getMedico() == null || cita.getMedico().getId_usuario() == 0) {
            ra.addFlashAttribute("err", "Debes seleccionar un médico.");
            return "redirect:/pedirCita";
        }
        if (cita.getFecha() == null) {
            ra.addFlashAttribute("err", "Debes seleccionar una fecha.");
            return "redirect:/pedirCita";
        }
        if (cita.getHora() == null) {
            ra.addFlashAttribute("err", "Debes seleccionar una hora.");
            return "redirect:/pedirCita";
        }
        if (cita.getDuracionMinutos() <= 0) {
            // si no viene informada, ponemos un valor razonable (se puede leer del global si prefieres)
            cita.setDuracionMinutos(30);
        }
// ===== Validaciones mínimas =====
        if (cita.getMedico() == null || cita.getMedico().getId_usuario() == 0) {
            ra.addFlashAttribute("err", "Debes seleccionar un médico.");
            return "redirect:/pedirCita";
        }


        // no permitir pasado
        LocalDateTime inicio = LocalDateTime.of(cita.getFecha(), cita.getHora());
        if (inicio.isBefore(LocalDateTime.now())) {
            ra.addFlashAttribute("err", "No puedes reservar en el pasado.");
            return "redirect:/pedirCita";
        }

        // antelación mínima si hoy (opcional)
        if (cita.getFecha().isEqual(LocalDate.now())) {
            if (inicio.isBefore(LocalDateTime.now().plusMinutes(30))) {
                ra.addFlashAttribute("err", "Debes reservar con al menos 30 minutos de antelación.");
                return "redirect:/pedirCita";
            }
        }

        // ===== Validación de disponibilidad real =====
        // 🔧 RESOLVER EL MÉDICO DESDE LA BD
        int idMedico = cita.getMedico().getId_usuario();
        Usuarios medico = usuariosRepository.findById(idMedico).orElse(null);
        if (medico == null) {
            ra.addFlashAttribute("err", "El médico seleccionado no existe.");
            return "redirect:/pedirCita";
        }
        cita.setMedico(medico);

        // Guardar
        citasService.guardarCita(cita);
        ra.addFlashAttribute("ok", "Cita creada correctamente.");
        return "redirect:/pedirCita";
    }

    // ENDPOINT JSON PARA CALENDARIO DEL USUARIO LOGUEADO
    @GetMapping(value = "/api/citas/mias", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<EventoCalendarDTO> citasMias(@AuthenticationPrincipal UsuariosDetails principal) {
        Usuarios usuario = usuariosRepository.findById(principal.getUsuario().getId_usuario())
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        return citasService.obtenerCitasPorUsuario(usuario).stream()
                .map(c -> new EventoCalendarDTO(
                        c.getId_cita(),
                        c.getTitulo() != null ? c.getTitulo() : "Cita",
                        c.getFecha() + "T" + c.getHora(),
                        c.getFecha() + "T" + c.getHora().plusMinutes(c.getDuracionMinutos()),
                        c.getEstado() != null ? c.getEstado().name() : "SIN_ESTADO",
                        c.getMedico() != null ? c.getMedico().getId_usuario() : null,
                        (c.getMedico() != null && c.getMedico().getCliente() != null)
                                ? c.getMedico().getCliente().getNombre() + " " + c.getMedico().getCliente().getApellidos()
                                : "Médico no asignado",
                        c.getDescripcion() != null ? c.getDescripcion() : "Sin descripción"
                ))
                .toList();
    }
}