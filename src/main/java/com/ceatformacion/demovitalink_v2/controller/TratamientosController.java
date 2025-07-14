package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.TratamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public String mostrarFormularioConLista(Model model) {
        model.addAttribute("tratamiento", new Tratamientos());
        model.addAttribute("tratamientos", tratamientoService.listarTodos());
        return "tratamientos"; // Vista HTML unificada
    }

    @PostMapping("/guardar")
    public String guardarTratamiento(@ModelAttribute Tratamientos tratamiento,
                                     @AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        Optional<Usuarios> usuario = usuariosRepository.findByUsername(username);

        if (usuario.isPresent()) {
            tratamiento.setUsuario(usuario.orElse(null));
            tratamientoService.guardar(tratamiento);
        }
        return "redirect:/tratamientos";
    }

    //Mostrar tratamientos

}
