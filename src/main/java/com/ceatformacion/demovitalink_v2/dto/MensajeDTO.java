package com.ceatformacion.demovitalink_v2.dto;

import java.time.LocalDateTime;


public record MensajeDTO(
        Integer id,
        Integer convId,
        String remitenteNombre,
        String contenido,
        String tipo,
        String urlAdjunto,
        LocalDateTime creadoEn
) {}