package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.TratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Optional;

@Controller
@RequestMapping("/tratamientos")
public class TratamientosController {
    @Autowired
    private TratamientoService tratamientoService;

    @Autowired
    private UsuariosRepository usuariosRepository;

    @GetMapping
    public String mostrarFormularioTratamiento(Model model) {
        model.addAttribute("tratamiento", new Tratamientos());
        return "tratamientos";
    }

    @PostMapping("/guardar")
    public String guardarTratamiento(@ModelAttribute("tratamiento") Tratamientos tratamiento) {
        try {
            // Obtener usuario autenticado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            Usuarios usuario = usuariosRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Asignar usuario al tratamiento
            tratamiento.setUsuario(usuario);

            tratamientoService.guardar(tratamiento);

            return "redirect:/tratamientos?success=true";
        } catch (Exception e) {
            return "redirect:/tratamientos?error=" + e.getMessage();
        }
    }

}
