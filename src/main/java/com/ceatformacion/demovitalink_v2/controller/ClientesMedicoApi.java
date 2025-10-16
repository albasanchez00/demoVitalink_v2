package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.ClienteVinculadoDTO;
import com.ceatformacion.demovitalink_v2.services.ClienteVinculadoService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medico/clientes")
@PreAuthorize("hasRole('MEDICO')")
public class ClientesMedicoApi {

    private final ClienteVinculadoService service;

    public ClientesMedicoApi(ClienteVinculadoService service) { this.service = service; }

    @GetMapping
    public Page<ClienteVinculadoDTO> listar(
            @AuthenticationPrincipal UsuariosDetails me,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "ultimaConsulta,desc") String sort,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String estado) {

        // Parseo del sort "campo,direccion"
        String[] s = sort.split(",");
        String campo = s[0];
        Sort.Direction dir = (s.length > 1 && "asc".equalsIgnoreCase(s[1])) ? Sort.Direction.ASC : Sort.Direction.DESC;

        // Evitar que JPA intente ordenar por un campo no mapeado en la entidad (ultimaConsulta)
        Sort sortJpa = Sort.unsorted();
        if (!"ultimaConsulta".equalsIgnoreCase(campo)) {
            // (Opcional) lista blanca para evitar campos inválidos: nombre, apellidos, correoElectronico, fechaNacimiento, etc.
            sortJpa = Sort.by(new Sort.Order(dir, campo));
        }

        Pageable pageable = PageRequest.of(page, size, sortJpa);

        // id del médico logado
        Integer medicoId = me.getUsuario().getId_usuario(); // o me.getId() si lo prefieres

        return service.buscarVinculados(medicoId, q, estado, pageable);
    }
}