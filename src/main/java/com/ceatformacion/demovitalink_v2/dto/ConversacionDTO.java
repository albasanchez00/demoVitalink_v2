package com.ceatformacion.demovitalink_v2.dto;

import com.ceatformacion.demovitalink_v2.model.Conversacion;

public record ConversacionDTO(
        Integer id,
        String tipo,
        String servicio,
        int miembrosCount,
        Boolean muted,      // ✅ AÑADIR
        Boolean archived    // ✅ AÑADIR
) {
    public static ConversacionDTO of(Conversacion c) {
        String tipo = c.getTipo() == null || c.getTipo().isBlank() ? "DIRECT" : c.getTipo();
        String servicio = c.getServicio() == null || c.getServicio().isBlank() ? "CHAT" : c.getServicio();
        int miembros = (c.getMiembros() != null ? c.getMiembros().size() : 0);

        return new ConversacionDTO(
                c.getId(),
                tipo,
                servicio,
                miembros,
                c.getMuted() != null ? c.getMuted() : false,     // ✅ AÑADIR
                c.getArchived() != null ? c.getArchived() : false // ✅ AÑADIR
        );
    }
}