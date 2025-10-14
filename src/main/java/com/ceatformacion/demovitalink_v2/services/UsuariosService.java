package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.utils.PasswordGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

public class UsuariosService {
    private final UsuariosRepository usuariosRepository;
    private final PasswordEncoder passwordEncoder;

    public record CredencialesDTO(String username, String passwordPlano) {}

    public UsuariosService(UsuariosRepository usuariosRepository,
                           PasswordEncoder passwordEncoder) {
        this.usuariosRepository = usuariosRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean existeUsername(String username) {
        return usuariosRepository.existsByUsernameIgnoreCase(username);
    }

    @Transactional
    public CredencialesDTO crearUsuarioParaCliente(Clientes cliente, Rol rol, String emailNormalizado) {
        String passwordPlano = PasswordGenerator.generar(10);

        Usuarios u = new Usuarios();
        u.setUsername(emailNormalizado);
        u.setPassword(passwordEncoder.encode(passwordPlano));
        u.setRol(rol);                 // Rol.USER / Rol.MEDICO / Rol.ADMIN
        u.setCliente(cliente);

        usuariosRepository.save(u);
        return new CredencialesDTO(u.getUsername(), passwordPlano);
    }

    @Transactional
    public void cambiarPassword(int usuarioId, String nuevaPasswordPlano) {
        Usuarios u = usuariosRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        u.setPassword(passwordEncoder.encode(nuevaPasswordPlano));
        usuariosRepository.save(u);
    }

    @Transactional
    public void asignarRol(int usuarioId, Rol nuevoRol) {
        Usuarios u = usuariosRepository.findById(usuarioId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
        u.setRol(nuevoRol);
        usuariosRepository.save(u);
    }
}
