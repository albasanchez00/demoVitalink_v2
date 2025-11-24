package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.CitasService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;

@Controller
public class CitasVistaController {

    private final CitasService citasService;
    private final UsuariosRepository usuariosRepository;

    public CitasVistaController(CitasService citasService, UsuariosRepository usuariosRepository) {
        this.citasService = citasService;
        this.usuariosRepository = usuariosRepository;
    }

    /**
     * Ver detalle de una cita específica
     * Solo el propietario o un médico/admin pueden verlo
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/citas/{id}")
    public String verDetalleCita(
            @PathVariable("id") int idCita,
            @AuthenticationPrincipal UsuariosDetails auth,
            Model model,
            RedirectAttributes ra
    ) {
        // Buscar la cita usando el repository o service
        Citas cita = citasService.obtenerCitasPorUsuario(auth.getUsuario())
                .stream()
                .filter(c -> c.getId_cita() == idCita)
                .findFirst()
                .orElse(null);

        // Si no es el propietario, intentar buscarla si es médico/admin
        if (cita == null) {
            String rol = auth.getUsuario().getRol().name();
            if (rol.equals("MEDICO") || rol.equals("ADMIN")) {
                // Buscar en todas las citas (necesitarás este método)
                // Por ahora usamos un approach alternativo
            }
            ra.addFlashAttribute("error", "La cita no existe o no tienes permisos para verla");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar permisos
        String rol = auth.getUsuario().getRol().name();
        boolean esPropietario = cita.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();
        boolean esMedicoOAdmin = rol.equals("MEDICO") || rol.equals("ADMIN");

        if (!esPropietario && !esMedicoOAdmin) {
            ra.addFlashAttribute("error", "No tienes permisos para ver esta cita");
            return "redirect:/usuarios/historialPaciente";
        }

        model.addAttribute("cita", cita);
        model.addAttribute("soloLectura", !esPropietario);
        return "cita-detalle";
    }

    /**
     * Formulario de edición de cita
     * Solo el propietario puede editar
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/citas/{id}/editar")
    public String editarCita(
            @PathVariable("id") int idCita,
            @AuthenticationPrincipal UsuariosDetails auth,
            Model model,
            RedirectAttributes ra
    ) {
        // Buscar la cita
        Citas cita = citasService.obtenerPorId(idCita).orElse(null);

        if (cita == null) {
            ra.addFlashAttribute("error", "La cita no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda editar
        boolean esPropietario = cita.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para editar esta cita");
            return "redirect:/citas/" + idCita;
        }

        // Pasar lista de médicos para el select
        model.addAttribute("cita", cita);
        model.addAttribute("medicos", usuariosRepository.findByRol(Rol.MEDICO));
        return "cita-editar";
    }

    /**
     * Guardar cambios de la cita editada
     * Solo el propietario puede actualizar
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/citas/{id}/editar")
    public String guardarEdicionCita(
            @PathVariable("id") int idCita,
            @ModelAttribute Citas citaForm,
            @AuthenticationPrincipal UsuariosDetails auth,
            RedirectAttributes ra
    ) {
        // Buscar la cita existente
        Citas citaExistente = citasService.obtenerPorId(idCita).orElse(null);

        if (citaExistente == null) {
            ra.addFlashAttribute("error", "La cita no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda editar
        boolean esPropietario = citaExistente.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para editar esta cita");
            return "redirect:/citas/" + idCita;
        }

        // Validar que no sea en el pasado
        LocalDateTime fechaHoraCita = LocalDateTime.of(citaForm.getFecha(), citaForm.getHora());
        if (fechaHoraCita.isBefore(LocalDateTime.now())) {
            ra.addFlashAttribute("error", "No puedes programar una cita en el pasado");
            return "redirect:/citas/" + idCita + "/editar";
        }

        // Actualizar solo los campos editables
        citaExistente.setTitulo(citaForm.getTitulo());
        citaExistente.setDescripcion(citaForm.getDescripcion());
        citaExistente.setFecha(citaForm.getFecha());
        citaExistente.setHora(citaForm.getHora());
        citaExistente.setDuracionMinutos(citaForm.getDuracionMinutos());

        // Actualizar médico si se cambió
        if (citaForm.getMedico() != null && citaForm.getMedico().getId_usuario() != 0) {
            citaExistente.setMedico(citaForm.getMedico());
        }

        // Guardar cambios
        citasService.guardarCita(citaExistente);

        ra.addFlashAttribute("success", "Cita actualizada correctamente");
        return "redirect:/citas/" + idCita;
    }

    /**
     * Eliminar cita
     * Solo el propietario puede eliminar
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/citas/{id}/eliminar")
    public String eliminarCita(
            @PathVariable("id") int idCita,
            @AuthenticationPrincipal UsuariosDetails auth,
            RedirectAttributes ra
    ) {
        // Buscar la cita
        Citas cita = citasService.obtenerPorId(idCita).orElse(null);

        if (cita == null) {
            ra.addFlashAttribute("error", "La cita no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda eliminar
        boolean esPropietario = cita.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para eliminar esta cita");
            return "redirect:/citas/" + idCita;
        }

        // Eliminar
        citasService.eliminar(idCita);

        ra.addFlashAttribute("success", "Cita eliminada correctamente");
        return "redirect:/usuarios/historialPaciente";
    }
}