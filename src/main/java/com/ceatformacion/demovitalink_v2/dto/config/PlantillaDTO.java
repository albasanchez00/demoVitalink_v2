package com.ceatformacion.demovitalink_v2.dto.config;

public record PlantillaDTO(
        Long id,           // null para nuevas
        String tipo,       // INFORME | MENSAJE | OTRO
        String nombre,
        String contenido
) {}