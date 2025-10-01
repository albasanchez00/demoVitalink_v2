package com.ceatformacion.demovitalink_v2.security;

import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("sec")
public class SecurityExpressions {

    public boolean isOwner(Integer userIdPath, Authentication auth) {
        if (userIdPath == null || auth == null || !(auth.getPrincipal() instanceof UsuariosDetails p)) {
            return false;
        }
        Integer currentId = p.getUsuario().getId_usuario();
        return userIdPath.equals(currentId);
    }

    // Útil si alguna vez quieres chequear “propietario” contra el propio principal sin path
    public boolean isSelf(Authentication auth, Integer userId) {
        if (auth == null || !(auth.getPrincipal() instanceof UsuariosDetails p) || userId == null) return false;
        return userId.equals(p.getUsuario().getId_usuario());
    }
}
