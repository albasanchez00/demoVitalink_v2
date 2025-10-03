package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
@Service
public class UsuariosDetailsServices implements UserDetailsService {
    private final UsuariosRepository usuariosRepository;

    //Constructor
    public UsuariosDetailsServices(UsuariosRepository usuariosRepository) {
        this.usuariosRepository = usuariosRepository;
    }

    //Metodo que devuelve los datos del usuario
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //Busca el usuario en la base de datos
        return usuariosRepository.findByUsername(username).map(UsuariosDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }
}
