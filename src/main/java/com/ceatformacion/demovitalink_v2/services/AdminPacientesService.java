package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.PacienteListDTO;
import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ClientesRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminPacientesService {
    private final ClientesRepository clientesRepo;
    private final UsuariosRepository usuariosRepo;
    private final AuditLogger audit;

    public AdminPacientesService(ClientesRepository c, UsuariosRepository u, AuditLogger a) {
        this.clientesRepo = c; this.usuariosRepo = u; this.audit = a;
    }

    @Transactional(readOnly = true)
    public Page<PacienteListDTO> listarPacientesDTO(String q, Pageable pageable) {
        Page<Clientes> page = (q == null || q.isBlank())
                ? clientesRepo.findAll(pageable)
                : clientesRepo.findByNombreContainingIgnoreCaseOrApellidosContainingIgnoreCase(q.trim(), q.trim(), pageable);

        return page.map(c -> new PacienteListDTO(
                c.getIdCliente(),
                c.getNombre(),
                c.getApellidos(),
                c.getMedicoReferencia() != null ? c.getMedicoReferencia().getId_usuario() : null,
                c.getMedicoReferencia() != null ? c.getMedicoReferencia().getUsername() : null
        ));
    }

    @Transactional
    public void asignarMedico(Integer idCliente, Integer idUsuarioMedico) {
        Clientes cli = clientesRepo.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        Usuarios med = usuariosRepo.findById(idUsuarioMedico)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));
        if (med.getRol() != Rol.MEDICO) throw new IllegalArgumentException("El usuario indicado no es médico");
        cli.setMedicoReferencia(med);
        audit.log("ASSIGN_MEDICO", "Clientes", String.valueOf(idCliente), "medicoId=" + idUsuarioMedico);
    }
}
