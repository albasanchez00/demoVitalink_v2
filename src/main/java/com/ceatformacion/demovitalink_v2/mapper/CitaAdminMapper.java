package com.ceatformacion.demovitalink_v2.mapper;

import com.ceatformacion.demovitalink_v2.dto.CitaAdminDTO;
import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Usuarios;

public final class CitaAdminMapper {
    private CitaAdminMapper(){}

    public static CitaAdminDTO toDTO(Citas c){
        var p = c.getUsuario();
        var m = c.getMedico();
        return new CitaAdminDTO(
                c.getId_cita(),
                (p!=null? p.getId_usuario(): null),
                nombreUsuario(p),
                (m!=null? m.getId_usuario(): null),
                nombreUsuario(m),
                c.getTitulo(),
                (c.getEstado()!=null? c.getEstado().name(): null),
                c.getFecha(),
                c.getHora()
        );
    }

    private static String nombreUsuario(Usuarios u){
        if(u==null) return "—";
        var cli = u.getCliente();
        String base = (cli!=null)
                ? (safe(cli.getNombre()) + " " + safe(cli.getApellidos())).trim()
                : u.getUsername();
        return base.isBlank()? u.getUsername() : base;
    }
    private static String safe(String s){ return (s==null)? "" : s; }
}