package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.CitasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.ResponseEntity;

@Controller
public class CitasController {
    @Autowired
    private CitasService citasService;
    @Autowired
    private UsuariosRepository usuariosRepository;

    @GetMapping("/pedirCita")
    public String mostrarFormularioCita(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        getCitasPorUsuario(username);
        model.addAttribute("citas", new Citas());
        return "pedirCita";
    }

    @PostMapping("/guardarCitas")
    public String guardarCita(@ModelAttribute("citas") Citas cita) {
        try {
            // Obtener usuario autenticado (ejemplo con Spring Security)
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            Usuarios usuario = usuariosRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            cita.setUsuario(usuario);

            citasService.guardarCita(cita);
       //     getCitasPorUsuario(username);
            return "redirect:/pedirCita?success=true";
        } catch (Exception e) {
            return "redirect:/pedirCita?error=" + e.getMessage();
        }
    }

    public ResponseEntity<?> getCitasPorUsuario(@RequestParam String username) {
        try {
            Usuarios usuario = usuariosRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            List<Citas> citas = citasService.obtenerCitasPorUsuario(usuario);
            List<Map<String, Object>> citasMap = citas.stream()
                .map(this::convertirCitaAMap)
                .collect(Collectors.toList());
            System.out.println(Arrays.toString(citas.toArray()));
            return ResponseEntity.ok(citasMap);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private Map<String, Object> convertirCitaAMap(Citas cita) {
        Map<String, Object> citaMap = new HashMap<>();
        citaMap.put("title", cita.getTipo() + " - " + cita.getPaciente());
        String start = cita.getFecha().toString() + "T" + cita.getHora().toString();
        citaMap.put("start", start);
        System.out.println(start);
        String end = cita.getFecha().toString() + "T" + (cita.getHora().plusHours(1)).toString();
        citaMap.put("end", end);
        System.out.println(end);
        return citaMap;
    }


}