package com.ceatformacion.demovitalink_v2.dto.config;


import java.util.Map;

public record NotificacionesDTO(
        CanalesDTO canales,
        EventosDTO eventos,
        String silencioDesde,
        String silencioHasta,
        Map<String,String> plantillas // clave=evento, valor=texto con variables
) {}
