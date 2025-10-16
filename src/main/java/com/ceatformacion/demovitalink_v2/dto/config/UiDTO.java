package com.ceatformacion.demovitalink_v2.dto.config;

public record UiDTO(
        String tema,        // auto|light|dark
        String idioma,      // es|en
        String zonaHoraria, // Europe/Madrid
        String home         // dashboard|agenda|mensajes
) {}