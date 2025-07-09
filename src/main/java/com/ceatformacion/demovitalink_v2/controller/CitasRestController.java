package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.services.CitasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/citas")
public class CitasRestController {
    @Autowired
    private CitasService citasService;

    @GetMapping
    public List<Map<String, Object>> getCitasPorUsuario(@RequestParam Usuarios usuario) {
        CitasService citaService;
        List<Citas> citas = citasService.obtenerCitasPorUsuario(usuario);

        return citas.stream().map(c -> {
            Map<String, Object> map = new HashMap<>();
            map.put("title", c.getTipo() + " - " + c.getPaciente());
            String start = c.getFecha().toString() + "T" + c.getHora().toString();
            map.put("start",start); // formato ISO 8601 para calendarios
            String end = c.getFecha().toString() + "T" + (c.getHora().plusHours(1)).toString();
            map.put("end",end); // formato ISO 8601 para calendarios
            map.put("id", c.getId_cita()); // por si luego necesitas editar/eliminar
            return map;
        }).collect(Collectors.toList());
    }
}
