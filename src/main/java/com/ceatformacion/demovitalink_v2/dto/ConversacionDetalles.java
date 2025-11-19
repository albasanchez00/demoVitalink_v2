package com.ceatformacion.demovitalink_v2.dto;

import java.time.LocalDateTime;
import java.util.List;

public record ConversacionDetalles(
        Integer id,
        String tipo,
        String servicio,
        Boolean muted,
        Boolean archived,
        LocalDateTime creadoEn,
        List<String> miembros,
        long totalMensajes,
        long noLeidos
) {}
