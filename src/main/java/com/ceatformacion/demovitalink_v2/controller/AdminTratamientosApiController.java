package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.TratamientoAdminDTO;
import com.ceatformacion.demovitalink_v2.mapper.TratamientoAdminMapper;
import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.services.TratamientoService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tratamientos")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTratamientosApiController {

    private final TratamientoService service;
    public AdminTratamientosApiController(TratamientoService service){ this.service = service; }

    @GetMapping
    public Page<TratamientoAdminDTO> listar(
            @RequestParam(value="q", required=false) String q,
            @RequestParam(value="estado", required=false) String estado,
            @RequestParam(value="idUsuario", required=false) Integer idUsuario,
            @RequestParam(value="page", defaultValue="0") int page,
            @RequestParam(value="size", defaultValue="10") int size,
            @RequestParam(value="sort", defaultValue="id_tratamiento,desc") String sortStr
    ){
        String[] parts = sortStr.split(",");
        Sort sort = Sort.by(Sort.Direction.fromString(parts.length>1?parts[1]:"desc"), parts[0]);
        Page<Tratamientos> p = service.buscarAdmin(q, estado, idUsuario, PageRequest.of(page, size, sort));
        return p.map(TratamientoAdminMapper::toDTO);
    }

    @GetMapping("/{id}")
    public TratamientoAdminDTO ver(@PathVariable Integer id) {
        return service.buscarPorId(id).map(TratamientoAdminMapper::toDTO)
                .orElseThrow(() -> new IllegalArgumentException("Tratamiento no encontrado"));
    }

    @DeleteMapping("/{id}")
    public void inactivar(@PathVariable Integer id){
        // si prefieres INACTIVO en vez de FINALIZADO, crea método service.inactivar(id)
        service.finalizar(id);
    }
}