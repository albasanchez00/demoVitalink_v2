package com.ceatformacion.demovitalink_v2.mapper;

import com.ceatformacion.demovitalink_v2.dto.TratamientoAdminDTO;
import com.ceatformacion.demovitalink_v2.model.Clientes;
import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.model.Usuarios;

public final class TratamientoAdminMapper {
    private TratamientoAdminMapper(){}

    public static TratamientoAdminDTO toDTO(Tratamientos t){
        Usuarios u = t.getUsuario();

        Integer usuarioId = (u != null) ? u.getId_usuario() : null;
        String usuarioNombre;
        if (u != null && u.getCliente() != null) {
            Clientes c = u.getCliente();
            String nom = c.getNombre() != null ? c.getNombre() : "";
            String ape = c.getApellidos() != null ? c.getApellidos() : "";
            usuarioNombre = (nom + " " + ape).trim();
            if (usuarioNombre.isEmpty() && u.getUsername() != null) usuarioNombre = u.getUsername();
        } else {
            usuarioNombre = (u != null && u.getUsername() != null) ? u.getUsername() : "—";
        }

        return new TratamientoAdminDTO(
                t.getId_tratamiento(),
                usuarioId,
                usuarioNombre,
                // <- mapeamos al nuevo nombre del DTO
                t.getNombre_tratamiento(),
                t.getEstado_tratamiento(),
                t.getFecha_inicio(),
                t.getFecha_fin()
        );
    }
}