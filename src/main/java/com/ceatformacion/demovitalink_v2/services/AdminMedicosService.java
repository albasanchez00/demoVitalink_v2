package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.MedicoListDTO;
import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminMedicosService {

    private final UsuariosRepository usuariosRepo;
    private final PasswordEncoder encoder;
    private final AuditLogger audit;

    public AdminMedicosService(UsuariosRepository repo, PasswordEncoder enc, AuditLogger audit) {
        this.usuariosRepo = repo;
        this.encoder = enc;
        this.audit = audit;
    }

    @Transactional(readOnly = true)
    public Page<MedicoListDTO> listarMedicosDTO(String q, Pageable pageable) {
        Page<Usuarios> page;

        if (q == null || q.isBlank()) {
            // Sin sort en el pageable: el ORDER BY lo impone la @Query del repositorio (id_usuario DESC)
            PageRequest noSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
            page = usuariosRepo.findMedicosOrdenFijo(Rol.MEDICO, noSort);
        } else {
            // Búsqueda por username + orden estable por username
            PageRequest byUsernameAsc = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("username").ascending()
            );
            page = usuariosRepo.findByRolAndUsernameContainingIgnoreCase(
                    Rol.MEDICO, q.trim(), byUsernameAsc
            );
        }

        // Map a DTO usando el getter real del id (id_usuario)
        return page.map(u -> new MedicoListDTO(u.getId_usuario(), u.getUsername()));
    }

    @Transactional
    public Integer crearMedico(String username, String rawPassword) {
        if (usuariosRepo.existsByUsernameIgnoreCase(username)) {
            throw new IllegalArgumentException("El username ya existe.");
        }
        Usuarios u = new Usuarios();
        u.setUsername(username);
        u.setPassword(encoder.encode(rawPassword));
        u.setRol(Rol.MEDICO);

        Integer id = usuariosRepo.save(u).getId_usuario();
        audit.log("CREATE", "Usuarios(MEDICO)", String.valueOf(id), username);
        return id;
    }

    @Transactional
    public void eliminarMedico(Integer id) {
        usuariosRepo.deleteById(id);
        audit.log("DELETE", "Usuarios(MEDICO)", String.valueOf(id), "");
    }
}
