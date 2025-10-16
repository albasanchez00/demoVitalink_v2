package com.ceatformacion.demovitalink_v2.dto.config;

import java.util.List;
import java.util.Map;

public record AgendaDTO(
        Integer duracionGeneralMin,
        Integer bufferMin,
        ReglasDTO reglas,
        Map<String, List<BloqueDTO>> disponibilidad,   // "lunes":[{desde,hasta,ubicacion}]
        Map<String,String> instruccionesPorTipo
) {}