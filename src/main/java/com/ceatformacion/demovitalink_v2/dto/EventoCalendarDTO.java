package com.ceatformacion.demovitalink_v2.dto;

public record EventoCalendarDTO(
        int id,
        String title,
        String start,       // "yyyy-MM-dd'T'HH:mm:ss"
        String end,         // idem
        String estado,
        Integer pacienteId,
        String pacienteNombre,
        String descripcion
) {}
