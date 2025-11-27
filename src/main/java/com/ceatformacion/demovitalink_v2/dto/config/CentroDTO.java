package com.ceatformacion.demovitalink_v2.dto.config;

import java.util.Map;

public record CentroDTO(
        String nombreCentro,
        String telefonoCentro,
        String direccionCentro,
        String horarioCentro,
        Map<String, Boolean> servicios  // {"cardiologia": true, "pediatria": true, ...}
) {}