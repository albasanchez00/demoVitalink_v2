package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;
@Controller
public class CitasAdminController {
    @Autowired
    private CitasRepository citasRepository;

    @Autowired
    private UsuariosRepository usuariosRepository;

    // ✅ Mostrar tabla con usuarios y botón "Ver Citas"
    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/citasCliente")
    public String mostrarCitasClientes(Model model) {
        // Obtener todas las citas de la base de datos
        List<Citas> citas = citasRepository.findAll();

        // Añadir al modelo
        model.addAttribute("citas", citas);

        return "citasCliente"; // Vista que muestra la tabla de citas
    }

    // ✅ Mostrar las citas de un usuario específico
    @PreAuthorize("hasRole('Admin')")
    @GetMapping("/agendaCitas")
    public String mostrarCitasPorUsuario(@RequestParam int idUsuario, Model model) {
        Optional<Usuarios> usuarioOpt = usuariosRepository.findById(idUsuario);

        if (usuarioOpt.isEmpty()) {
            model.addAttribute("error", "Usuario no encontrado");
            return "error"; // Página de error
        }

        Usuarios usuario = usuarioOpt.get();

        // Validar que el usuario tiene cliente asociado
        if (usuario.getCliente() == null) {
            model.addAttribute("error", "Este usuario no tiene cliente vinculado.");
            return "error";
        }

        List<Citas> citas = citasRepository.findCitasByUsuario(usuario);

        model.addAttribute("usuario", usuario);
        model.addAttribute("citas", citas);

        return "agendaCitas";
    }


}
