package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.TratamientoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

// MedicoTratamientosController.java
// MedicoTratamientosController.java
@Controller
@RequestMapping("/medico")
public class MedicoTratamientosController {
    private final TratamientoService tratamientoService;
    private final UsuariosRepository usuariosRepository;

    public MedicoTratamientosController(TratamientoService tratamientoService,
                                        UsuariosRepository usuariosRepository) {
        this.tratamientoService = tratamientoService;
        this.usuariosRepository = usuariosRepository;
    }

    // LISTADO (con filtro opcional por id_usuario)
    @GetMapping("/tratamientos")
    public String verTratamientosMedico(@RequestParam(value = "id_usuario", required = false) Integer idUsuario,
                                        Model model) {
        model.addAttribute("usuarios", usuariosRepository.findAll()); // para el <select>

        if (idUsuario != null) {
            List<Tratamientos> lista = tratamientoService.obtenerTratamientosPorIdUsuario(idUsuario);
            model.addAttribute("tratamientos", lista);
            usuariosRepository.findById(idUsuario).ifPresent(u -> model.addAttribute("usuario", u)); // para mostrar "de Nombre Apellidos"
            model.addAttribute("idSeleccionado", idUsuario); // para marcar el option seleccionado
        } else {
            model.addAttribute("tratamientos", tratamientoService.obtenerTodos());
        }

        return "tratamientosMedicos";
    }

    // FORM NUEVO (con o sin ?id_usuario=)
    @GetMapping("/nuevo")
    public String nuevoTratamiento(@RequestParam(value = "id_usuario", required = false) Integer idUsuario,
                                   Model model) {
        Tratamientos t = new Tratamientos();
        if (idUsuario != null) {
            Usuarios u = usuariosRepository.findById(idUsuario)
                    .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
            t.setUsuario(u);
            model.addAttribute("usuario", u);
        } else {
            model.addAttribute("usuarios", usuariosRepository.findAll());
        }
        model.addAttribute("tratamiento", t);
        return "tratamientoNuevo";
    }

    // GUARDAR
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("tratamiento") Tratamientos tratamiento,
                          @RequestParam("id_usuario") Integer idUsuario) {
        Usuarios u = usuariosRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        tratamiento.setUsuario(u);
        tratamientoService.guardar(tratamiento);

        // volver al listado filtrado por ese usuario
        return "redirect:/medico/tratamientos?id_usuario=" + idUsuario + "&success=true";
    }
}