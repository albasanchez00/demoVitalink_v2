package com.ceatformacion.demovitalink_v2.dto;

import java.time.LocalDate;

public record EstadisticasDebugDTO(
        int id_usuario,
        long sintomasTotal, LocalDate sintomasMin, LocalDate sintomasMax,
        long citasTotal, LocalDate citasMin, LocalDate citasMax,
        long tratamTotal
) {}
