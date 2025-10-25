package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.services.DisponibilidadService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/medico/{medicoId}/disponibilidad")
@PreAuthorize("hasAnyRole('ADMIN','MEDICO')")
public class DisponibilidadRestController {

    private final DisponibilidadService service;

    public DisponibilidadRestController(DisponibilidadService service) { this.service = service; }

    @GetMapping
    public List<String> horas(@PathVariable int medicoId,
                              @RequestParam String fecha,           // "YYYY-MM-DD"
                              @RequestParam(required = false) Integer slotMinutos) {
        return service.calcularHorasDisponibles(medicoId, LocalDate.parse(fecha), slotMinutos);
    }
}