package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.services.TratamientoService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class TratamientosVistaController {

    private final TratamientoService tratamientoService;

    public TratamientosVistaController(TratamientoService tratamientoService) {
        this.tratamientoService = tratamientoService;
    }

    /**
     * Ver detalle de un tratamiento específico
     * Solo el propietario o un médico/admin pueden verlo
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tratamientos/{id}")
    public String verDetalleTratamiento(
            @PathVariable("id") int idTratamiento,
            @AuthenticationPrincipal UsuariosDetails auth,
            Model model,
            RedirectAttributes ra
    ) {
        // Buscar el tratamiento
        Tratamientos tratamiento = tratamientoService.obtenerPorId(idTratamiento)
                .orElse(null);

        if (tratamiento == null) {
            ra.addFlashAttribute("error", "El tratamiento no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar permisos: solo el dueño o médicos/admins pueden ver
        String rol = auth.getUsuario().getRol().name();
        boolean esPropietario = tratamiento.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();
        boolean esMedicoOAdmin = rol.equals("MEDICO") || rol.equals("ADMIN");

        if (!esPropietario && !esMedicoOAdmin) {
            ra.addFlashAttribute("error", "No tienes permisos para ver este tratamiento");
            return "redirect:/usuarios/historialPaciente";
        }

        model.addAttribute("tratamiento", tratamiento);
        model.addAttribute("soloLectura", !esPropietario); // Los médicos ven en solo lectura
        return "tratamiento-detalle";
    }

    /**
     * Formulario de edición de tratamiento
     * Solo el propietario puede editar
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/tratamientos/{id}/editar")
    public String editarTratamiento(
            @PathVariable("id") int idTratamiento,
            @AuthenticationPrincipal UsuariosDetails auth,
            Model model,
            RedirectAttributes ra
    ) {
        // Buscar el tratamiento
        Tratamientos tratamiento = tratamientoService.obtenerPorId(idTratamiento)
                .orElse(null);

        if (tratamiento == null) {
            ra.addFlashAttribute("error", "El tratamiento no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda editar
        boolean esPropietario = tratamiento.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para editar este tratamiento");
            return "redirect:/tratamientos/" + idTratamiento;
        }

        model.addAttribute("tratamiento", tratamiento);
        return "tratamiento-editar";
    }

    /**
     * Guardar cambios del tratamiento editado
     * Solo el propietario puede actualizar
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tratamientos/{id}/editar")
    public String guardarEdicionTratamiento(
            @PathVariable("id") int idTratamiento,
            @ModelAttribute Tratamientos tratamientoForm,
            @AuthenticationPrincipal UsuariosDetails auth,
            RedirectAttributes ra
    ) {
        // Buscar el tratamiento existente
        Tratamientos tratamientoExistente = tratamientoService.obtenerPorId(idTratamiento)
                .orElse(null);

        if (tratamientoExistente == null) {
            ra.addFlashAttribute("error", "El tratamiento no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda editar
        boolean esPropietario = tratamientoExistente.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para editar este tratamiento");
            return "redirect:/tratamientos/" + idTratamiento;
        }

        // Actualizar solo los campos editables (mantener usuario y fecha de inicio originales)
        tratamientoExistente.setNombre_tratamiento(tratamientoForm.getNombre_tratamiento());
        tratamientoExistente.setDosis(tratamientoForm.getDosis());
        tratamientoExistente.setFrecuencia(tratamientoForm.getFrecuencia());
        tratamientoExistente.setFormula(tratamientoForm.getFormula());
        tratamientoExistente.setObservaciones(tratamientoForm.getObservaciones());
        tratamientoExistente.setEstado_tratamiento(tratamientoForm.getEstado_tratamiento());
        tratamientoExistente.setToma_alimentos(tratamientoForm.isToma_alimentos());
        tratamientoExistente.setFecha_fin(tratamientoForm.getFecha_fin());
        tratamientoExistente.setDuracion(tratamientoForm.getDuracion());
        tratamientoExistente.setSintomas(tratamientoForm.getSintomas());

        // Guardar cambios
        tratamientoService.guardar(tratamientoExistente);

        ra.addFlashAttribute("success", "Tratamiento actualizado correctamente");
        return "redirect:/tratamientos/" + idTratamiento;
    }

    /**
     * Finalizar tratamiento (cambiar estado a FINALIZADO)
     * Solo el propietario puede finalizar
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tratamientos/{id}/finalizar")
    public String finalizarTratamiento(
            @PathVariable("id") int idTratamiento,
            @AuthenticationPrincipal UsuariosDetails auth,
            RedirectAttributes ra
    ) {
        // Buscar el tratamiento
        Tratamientos tratamiento = tratamientoService.obtenerPorId(idTratamiento)
                .orElse(null);

        if (tratamiento == null) {
            ra.addFlashAttribute("error", "El tratamiento no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda finalizar
        boolean esPropietario = tratamiento.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para finalizar este tratamiento");
            return "redirect:/tratamientos/" + idTratamiento;
        }

        // Finalizar
        tratamientoService.finalizar(idTratamiento);

        ra.addFlashAttribute("success", "Tratamiento finalizado correctamente");
        return "redirect:/tratamientos/" + idTratamiento;
    }

    /**
     * Eliminar tratamiento
     * Solo el propietario puede eliminar
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/tratamientos/{id}/eliminar")
    public String eliminarTratamiento(
            @PathVariable("id") int idTratamiento,
            @AuthenticationPrincipal UsuariosDetails auth,
            RedirectAttributes ra
    ) {
        // Buscar el tratamiento
        Tratamientos tratamiento = tratamientoService.obtenerPorId(idTratamiento)
                .orElse(null);

        if (tratamiento == null) {
            ra.addFlashAttribute("error", "El tratamiento no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda eliminar
        boolean esPropietario = tratamiento.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para eliminar este tratamiento");
            return "redirect:/tratamientos/" + idTratamiento;
        }

        // Eliminar
        tratamientoService.eliminar(idTratamiento);

        ra.addFlashAttribute("success", "Tratamiento eliminado correctamente");
        return "redirect:/usuarios/historialPaciente";
    }
}