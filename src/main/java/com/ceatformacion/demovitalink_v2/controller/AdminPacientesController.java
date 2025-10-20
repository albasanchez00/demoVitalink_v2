package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.PacienteListDTO;
import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import com.ceatformacion.demovitalink_v2.services.AdminPacientesService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/pacientes")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPacientesController {
    private final AdminPacientesService service;
    private final ClientesRepository clientesRepo;

    public AdminPacientesController(AdminPacientesService service, ClientesRepository clientesRepo) {
        this.service = service;
        this.clientesRepo = clientesRepo;
    }

    @GetMapping
    public ResponseEntity<Page<PacienteListDTO>> listar(
            @RequestParam(required=false) String q,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(service.listarPacientesDTO(q, pageable));
    }

    @PatchMapping("/{idCliente}/asignar-medico/{idUsuarioMedico}")
    public ResponseEntity<Void> asignar(@PathVariable Integer idCliente, @PathVariable Integer idUsuarioMedico) {
        service.asignarMedico(idCliente, idUsuarioMedico);
        return ResponseEntity.noContent().build();
    }
    @Transactional(readOnly = true)
    public Page<PacienteListDTO> listarPacientesDTO(String q, Pageable pageable) {
        Page<Clientes> page = (q == null || q.isBlank())
                ? clientesRepo.findAll(pageable)
                : clientesRepo.findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(q, q, pageable);

        return page.map(c -> new PacienteListDTO(
                c.getIdCliente(),
                c.getNombre(),
                c.getApellidos(),
                c.getMedicoReferencia() != null ? c.getMedicoReferencia().getId_usuario() : null,
                c.getMedicoReferencia() != null ? c.getMedicoReferencia().getUsername() : null
        ));
    }
}
