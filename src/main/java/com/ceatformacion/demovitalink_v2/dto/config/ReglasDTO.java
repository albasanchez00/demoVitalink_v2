package com.ceatformacion.demovitalink_v2.dto.config;

public record ReglasDTO(
        Integer antelacionMinHoras,
        Integer cancelacionMinHoras,
        Boolean overbooking
) {}