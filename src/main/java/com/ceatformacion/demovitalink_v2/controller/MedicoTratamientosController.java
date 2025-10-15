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

    @GetMapping("/tratamientos")
    public String verTratamientosMedico(@RequestParam(value = "id_usuario", required = false) Integer idUsuario,
                                        Model model) {
        model.addAttribute("usuarios", usuariosRepository.findAll());
        if (idUsuario != null) {
            model.addAttribute("tratamientos", tratamientoService.obtenerTratamientosPorIdUsuario(idUsuario));
            usuariosRepository.findById(idUsuario).ifPresent(u -> model.addAttribute("usuario", u));
            model.addAttribute("idSeleccionado", idUsuario);
        } else {
            model.addAttribute("tratamientos", tratamientoService.obtenerTodos());
        }
        return "tratamientosMedicos";
    }

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

    // Soporte /medico/nuevo/{id}
    @GetMapping("/nuevo/{id}")
    public String nuevoTratamientoPath(@PathVariable("id") Integer idUsuario, Model model) {
        return nuevoTratamiento(idUsuario, model);
    }

    @PostMapping("/guardar")
    public String guardar(@ModelAttribute("tratamiento") Tratamientos tratamiento,
                          @RequestParam("id_usuario") Integer idUsuario) {
        Usuarios u = usuariosRepository.findById(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        tratamiento.setUsuario(u);
        tratamientoService.guardar(tratamiento);
        return "redirect:/medico/tratamientos?id_usuario=" + idUsuario + "&success=true";
    }

    @GetMapping("/editar/{id}")
    public String editarTratamiento(@PathVariable("id") Integer idTratamiento, Model model) {
        Tratamientos t = tratamientoService.buscarPorId(idTratamiento)
                .orElseThrow(() -> new IllegalArgumentException("Tratamiento no encontrado"));
        model.addAttribute("usuario", t.getUsuario());   // para el hidden id_usuario y el título
        model.addAttribute("tratamiento", t);
        return "tratamientoNuevo";
    }

    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute("tratamiento") Tratamientos form) {
        Tratamientos original = tratamientoService.buscarPorId(form.getId_tratamiento())
                .orElseThrow(() -> new IllegalArgumentException("Tratamiento no encontrado"));

        original.setNombre_tratamiento(form.getNombre_tratamiento());
        original.setFormula(form.getFormula());
        original.setDosis(form.getDosis());
        original.setFrecuencia(form.getFrecuencia());
        original.setFecha_inicio(form.getFecha_inicio());
        original.setFecha_fin(form.getFecha_fin());
        original.setToma_alimentos(form.isToma_alimentos());
        original.setEstado_tratamiento(form.getEstado_tratamiento());
        original.setSintomas(form.getSintomas());
        original.setObservaciones(form.getObservaciones());

        tratamientoService.guardar(original);

        Integer idUsuario = original.getUsuario().getId_usuario();
        return "redirect:/medico/tratamientos?id_usuario=" + idUsuario + "&updated=true";
    }
    // MedicoTratamientosController.java

    @PostMapping("/finalizar/{id}")
    public String finalizar(@PathVariable Integer id,
                            @RequestParam(value = "id_usuario", required = false) Integer idUsuario) {
        tratamientoService.finalizar(id);
        String suffix = (idUsuario != null) ? "?id_usuario="+idUsuario : "";
        return "redirect:/medico/tratamientos" + suffix + (suffix.isEmpty()? "?":"&") + "finalized=true";
    }

    @PostMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id,
                           @RequestParam(value = "id_usuario", required = false) Integer idUsuario) {
        tratamientoService.eliminar(id);
        String suffix = (idUsuario != null) ? "?id_usuario="+idUsuario : "";
        return "redirect:/medico/tratamientos" + suffix + (suffix.isEmpty()? "?":"&") + "deleted=true";
    }

}