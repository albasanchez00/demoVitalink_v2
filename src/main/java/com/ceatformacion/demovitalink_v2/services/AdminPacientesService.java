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

import java.util.Map;

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
    public PacienteListDTO asignarMedico(Integer idCliente, Integer idUsuarioMedico) {
        Clientes cli = clientesRepo.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        Usuarios med = usuariosRepo.findById(idUsuarioMedico)
                .orElseThrow(() -> new IllegalArgumentException("Médico no encontrado"));

        if (med.getRol() != Rol.MEDICO) {
            throw new IllegalArgumentException("El usuario indicado no es médico");
        }

        cli.setMedicoReferencia(med);
        clientesRepo.save(cli); // 👈 explícito

        audit.log("ASSIGN_MEDICO", "Clientes",
                String.valueOf(idCliente), "medicoId=" + idUsuarioMedico);

        // Devolver DTO actualizado
        return new PacienteListDTO(
                cli.getIdCliente(),
                cli.getNombre(),
                cli.getApellidos(),
                med.getId_usuario(),
                med.getUsername()
        );
    }

    @Transactional
    public void actualizarPaciente(Integer id, Map<String, Object> datos) {
        Clientes c = clientesRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));

        if (datos.containsKey("nombre"))
            c.setNombre((String) datos.get("nombre"));

        if (datos.containsKey("apellidos"))
            c.setApellidos((String) datos.get("apellidos"));

        if (datos.containsKey("telefono"))
            c.setTelefono((String) datos.get("telefono"));

        if (datos.containsKey("correoElectronico"))
            c.setCorreoElectronico((String) datos.get("correoElectronico"));

        if (datos.containsKey("direccion"))
            c.setDireccion((String) datos.get("direccion"));

        if (datos.containsKey("cp_id"))
            c.setCp_id((String) datos.get("cp_id"));

        if (datos.containsKey("prefTema"))
            c.setPrefTema((String) datos.get("prefTema"));

        if (datos.containsKey("prefNotificaciones"))
            c.setPrefNotificaciones((Boolean) datos.get("prefNotificaciones"));

        clientesRepo.save(c);
        audit.log("UPDATE", "Clientes(PACIENTE)", String.valueOf(id), c.getNombre() + " " + c.getApellidos());
    }
    @Transactional
    public void eliminarPaciente(Integer id) {
        Clientes c = clientesRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Paciente no encontrado"));
        clientesRepo.delete(c);
        audit.log("DELETE", "Clientes(PACIENTE)", String.valueOf(id),
                c.getNombre() + " " + c.getApellidos());
    }

}
