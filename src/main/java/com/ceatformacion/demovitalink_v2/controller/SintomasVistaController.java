package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.model.TipoSintoma;
import com.ceatformacion.demovitalink_v2.model.ZonaCorporal;
import com.ceatformacion.demovitalink_v2.services.SintomasService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SintomasVistaController {

    private final SintomasService sintomasService;

    public SintomasVistaController(SintomasService sintomasService) {
        this.sintomasService = sintomasService;
    }

    /**
     * Formulario de registro de síntomas (ya lo tenías)
     */
    @GetMapping("/registroSintomas")
    public String registroSintomas() {
        return "registroSintomas";
    }

    /**
     * Ver detalle de un síntoma específico
     * Solo el propietario o un médico/admin pueden verlo
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sintomas/{id}")
    public String verDetalleSintoma(
            @PathVariable("id") int idSintoma,
            @AuthenticationPrincipal UsuariosDetails auth,
            Model model,
            RedirectAttributes ra
    ) {
        // Buscar el síntoma
        Sintomas sintoma = sintomasService.obtenerPorId(idSintoma)
                .orElse(null);

        if (sintoma == null) {
            ra.addFlashAttribute("error", "El síntoma no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar permisos: solo el dueño o médicos/admins pueden ver
        String rol = auth.getUsuario().getRol().name();
        boolean esPropietario = sintoma.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();
        boolean esMedicoOAdmin = rol.equals("MEDICO") || rol.equals("ADMIN");

        if (!esPropietario && !esMedicoOAdmin) {
            ra.addFlashAttribute("error", "No tienes permisos para ver este síntoma");
            return "redirect:/usuarios/historialPaciente";
        }

        model.addAttribute("sintoma", sintoma);
        model.addAttribute("soloLectura", !esPropietario); // Los médicos ven en solo lectura
        return "sintoma-detalle";
    }

    /**
     * Formulario de edición de síntoma
     * Solo el propietario puede editar
     */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/sintomas/{id}/editar")
    public String editarSintoma(
            @PathVariable("id") int idSintoma,
            @AuthenticationPrincipal UsuariosDetails auth,
            Model model,
            RedirectAttributes ra
    ) {
        // Buscar el síntoma
        Sintomas sintoma = sintomasService.obtenerPorId(idSintoma)
                .orElse(null);

        if (sintoma == null) {
            ra.addFlashAttribute("error", "El síntoma no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda editar
        boolean esPropietario = sintoma.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para editar este síntoma");
            return "redirect:/sintomas/" + idSintoma;
        }

        // Pasar enums al modelo para los selects
        model.addAttribute("sintoma", sintoma);
        model.addAttribute("tiposSintoma", TipoSintoma.values());
        model.addAttribute("zonasCorporales", ZonaCorporal.values());
        return "sintoma-editar";
    }

    /**
     * Guardar cambios del síntoma editado
     * Solo el propietario puede actualizar
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/sintomas/{id}/editar")
    public String guardarEdicionSintoma(
            @PathVariable("id") int idSintoma,
            @ModelAttribute Sintomas sintomaForm,
            @AuthenticationPrincipal UsuariosDetails auth,
            RedirectAttributes ra
    ) {
        // Buscar el síntoma existente
        Sintomas sintomaExistente = sintomasService.obtenerPorId(idSintoma)
                .orElse(null);

        if (sintomaExistente == null) {
            ra.addFlashAttribute("error", "El síntoma no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda editar
        boolean esPropietario = sintomaExistente.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para editar este síntoma");
            return "redirect:/sintomas/" + idSintoma;
        }

        // Actualizar solo los campos editables (mantener usuario y fechaRegistro originales)
        sintomaExistente.setTipo(sintomaForm.getTipo());
        sintomaExistente.setZona(sintomaForm.getZona());
        sintomaExistente.setDescripcion(sintomaForm.getDescripcion());

        // Si el usuario cambió la fecha de registro, actualizarla
        if (sintomaForm.getFechaRegistro() != null) {
            sintomaExistente.setFechaRegistro(sintomaForm.getFechaRegistro());
        }

        // Guardar cambios
        sintomasService.actualizar(idSintoma, sintomaExistente);

        ra.addFlashAttribute("success", "Síntoma actualizado correctamente");
        return "redirect:/sintomas/" + idSintoma;
    }

    /**
     * Eliminar síntoma
     * Solo el propietario puede eliminar
     */
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/sintomas/{id}/eliminar")
    public String eliminarSintoma(
            @PathVariable("id") int idSintoma,
            @AuthenticationPrincipal UsuariosDetails auth,
            RedirectAttributes ra
    ) {
        // Buscar el síntoma
        Sintomas sintoma = sintomasService.obtenerPorId(idSintoma)
                .orElse(null);

        if (sintoma == null) {
            ra.addFlashAttribute("error", "El síntoma no existe");
            return "redirect:/usuarios/historialPaciente";
        }

        // Validar que solo el propietario pueda eliminar
        boolean esPropietario = sintoma.getUsuario().getId_usuario() == auth.getUsuario().getId_usuario();

        if (!esPropietario) {
            ra.addFlashAttribute("error", "No tienes permisos para eliminar este síntoma");
            return "redirect:/sintomas/" + idSintoma;
        }

        // Eliminar
        sintomasService.eliminar(idSintoma);

        ra.addFlashAttribute("success", "Síntoma eliminado correctamente");
        return "redirect:/usuarios/historialPaciente";
    }
}