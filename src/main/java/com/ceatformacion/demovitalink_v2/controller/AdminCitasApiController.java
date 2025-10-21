package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.CitaAdminDTO;
import com.ceatformacion.demovitalink_v2.mapper.CitaAdminMapper;
import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.EstadoCita;
import com.ceatformacion.demovitalink_v2.services.CitasAdminService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/admin/citas")
@PreAuthorize("hasRole('ADMIN')")
public class AdminCitasApiController {

    private final CitasAdminService service;

    public AdminCitasApiController(CitasAdminService service) {
        this.service = service;
    }

    @GetMapping
    public Page<CitaAdminDTO> list(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Integer idPaciente,
            @RequestParam(required = false) Integer idMedico,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fecha,desc") String sort
    ){
        Sort sortObj = Sort.by(
                sort.contains(",") ? Sort.Order.by(sort.split(",")[0]).with(
                        "desc".equalsIgnoreCase(sort.split(",")[1]) ? Sort.Direction.DESC : Sort.Direction.ASC
                ) : Sort.Order.desc("fecha")
        );
        Pageable pageable = PageRequest.of(page, size, sortObj);
        EstadoCita est = parseEstado(estado);

        Page<Citas> p = service.buscar(q, est, desde, hasta, idPaciente, idMedico, pageable);
        return p.map(CitaAdminMapper::toDTO);
    }

    @GetMapping("/{id}")
    public CitaAdminDTO one(@PathVariable Integer id){
        return CitaAdminMapper.toDTO(service.getById(id));
    }

    @DeleteMapping("/{id}")
    public void cancelar(@PathVariable Integer id){
        service.cancelar(id);
    }

    private EstadoCita parseEstado(String s){
        if(s==null || s.isBlank()) return null;
        try { return EstadoCita.valueOf(s.toUpperCase()); }
        catch (Exception e){ return null; }
    }
}