package com.ceatformacion.demovitalink_v2.mapper;

import com.ceatformacion.demovitalink_v2.dto.UsuarioLiteDTO;
import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Usuarios;

public final class UsuarioLiteMapper {
    private UsuarioLiteMapper(){}

    public static UsuarioLiteDTO toDTO(Usuarios u){
        String nombre = null;
        if (u.getCliente() != null) {
            Clientes c = u.getCliente();
            String n = c.getNombre() != null ? c.getNombre() : "";
            String a = c.getApellidos() != null ? c.getApellidos() : "";
            nombre = (n + " " + a).trim();
        }
        String user = (u.getUsername() != null) ? u.getUsername() : "";
        String etiqueta = (nombre != null && !nombre.isBlank())
                ? nombre + (user.isBlank() ? "" : " (" + user + ")")
                : (!user.isBlank() ? user : "Usuario");
        return new UsuarioLiteDTO(u.getId_usuario(), etiqueta + " — #" + u.getId_usuario());
    }
}