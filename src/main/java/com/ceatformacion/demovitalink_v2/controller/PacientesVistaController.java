package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/medico")
public class PacientesVistaController {

    /**
     * Vista principal de pacientes vinculados
     */
    @GetMapping("/pacientes")
    public String pacientesVinculados(Model model) {
        model.addAttribute("page", "pacientes");
        return "pacientesMedico";
    }

    /**
     * Ver perfil de un paciente específico
     * Ruta: /medico/clientes/{id}
     *
     * Redirige a la vista de edición de cliente existente
     */
    @GetMapping("/clientes/{id}")
    public String verPerfilPaciente(@PathVariable("id") Integer id) {
        // Redirige a la ruta existente de edición de clientes
        return "redirect:/clientes/editar/" + id;
    }

    /**
     * Ver historial médico de un paciente
     * Ruta: /medico/clientes/{id}/historial
     *
     * Redirige a la vista de historial pasando el ID del usuario
     */
    @GetMapping("/clientes/{id}/historial")
    public String verHistorialPaciente(@PathVariable("id") Integer id, Model model) {
        // Pasa el ID del paciente como atributo al modelo
        model.addAttribute("userId", id);
        model.addAttribute("page", "historial-paciente");
        // Redirige a la vista de historial
        return "redirect:/usuarios/historialPaciente?userId=" + id;
    }

    /**
     * Ver tratamientos de un paciente
     * Ruta: /medico/clientes/{id}/tratamientos
     *
     * Redirige a la vista de tratamientos existente con query param
     */
    @GetMapping("/clientes/{id}/tratamientos")
    public String verTratamientosPaciente(@PathVariable("id") Integer id) {
        // Redirige usando el query param que espera el controlador existente
        return "redirect:/medico/tratamientos?id_usuario=" + id;
    }
}