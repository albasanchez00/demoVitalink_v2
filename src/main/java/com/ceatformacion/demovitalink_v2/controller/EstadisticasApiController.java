package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.EstadisticasDebugDTO;
import com.ceatformacion.demovitalink_v2.dto.EstadisticasResponse;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.SintomasRepository;
import com.ceatformacion.demovitalink_v2.repository.TratamientosRepository;
import com.ceatformacion.demovitalink_v2.services.EstadisticasService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/estadisticas")
public class EstadisticasApiController {

    private final EstadisticasService estadisticasService;

    public EstadisticasApiController(EstadisticasService estadisticasService) {
        this.estadisticasService = estadisticasService;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public EstadisticasResponse obtener(
            @RequestParam(name = "id_usuario") int idUsuario,
            @RequestParam(name = "desde") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(name = "hasta") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(name = "tipo", defaultValue = "todos") String tipo
    ) {
        return estadisticasService.calcular(idUsuario, desde, hasta, tipo);
    }

}
