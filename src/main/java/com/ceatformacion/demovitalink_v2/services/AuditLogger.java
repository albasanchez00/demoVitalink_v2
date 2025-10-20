package com.ceatformacion.demovitalink_v2.services;


import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuditLogger {
    public void log(String accion, String entidad, String entidadId, String detalle) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String actor = (auth != null) ? auth.getName() : "anonymous";
        // TODO persistir en tabla 'auditoria' (actor, accion, entidad, entidadId, detalle, timestamp)
    }
}