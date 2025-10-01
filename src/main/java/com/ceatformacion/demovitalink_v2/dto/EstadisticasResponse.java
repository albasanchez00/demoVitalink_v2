package com.ceatformacion.demovitalink_v2.dto;

import java.util.List;

public record EstadisticasResponse(
        // Síntomas
        List<TimePointDTO> sintomasPorDia,
        List<CategoryValueDTO> sintomasPorTipo,

        // Citas
        List<TimePointDTO> citasPorDia,
        List<CategoryValueDTO> citasProximasVsPasadas,

        // Tratamientos
        List<CategoryValueDTO> tratamientosPorTipo
) {}
