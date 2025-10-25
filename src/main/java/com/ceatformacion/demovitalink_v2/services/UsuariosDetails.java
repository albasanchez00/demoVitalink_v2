package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UsuariosDetails implements UserDetails {
    private Usuarios usuario;
    //Constructor
    public UsuariosDetails(Usuarios usuario) {
        this.usuario = usuario;
    }
    public Usuarios getUsuario() {
        return this.usuario;
    }
    //Metodo que devuelve los datos del usuario
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        String authority = "ROLE_" + usuario.getRol().name(); // MEDICO -> ROLE_MEDICO
        return List.of(new SimpleGrantedAuthority(authority));
    }
    //Metodo que devuelve el password del usuario
    @Override
    public String getPassword() {
        return usuario.getPassword();
    }

    //Metodo que devuelve el username del usuario
    @Override
    public String getUsername() {
        return usuario.getUsername();
    }
    //Metodos que indican si el usuario esta habilitado o no
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
