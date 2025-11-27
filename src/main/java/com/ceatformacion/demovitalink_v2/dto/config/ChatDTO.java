package com.ceatformacion.demovitalink_v2.dto.config;

import java.util.Map;

public record ChatDTO(
        String estado,           // DISPONIBLE | OCUPADO | AUSENTE
        String firmaChat,        // "Dr./Dra. {nombre}"
        Map<String, String> respuestasRapidas  // {"hola": "Hola, ¿en qué puedo ayudarte?", ...}
) {}