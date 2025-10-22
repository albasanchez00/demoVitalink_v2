package com.ceatformacion.demovitalink_v2.dto.admin;

public record OverviewStatsDTO(
        long pacientesActivos,
        long medicosActivos,
        long tratamientosActivos,
        double adherenciaMedia,
        long citasSemana,
        Double esperaMediaDias

) {}