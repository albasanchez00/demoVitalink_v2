package com.ceatformacion.demovitalink_v2.dto;


import java.time.LocalDate;


public record TratamientoAdminDTO(
        Integer id,
        Integer usuarioId,
        String  usuarioNombre,
        String  nombreTratamiento,  // <- antes "diagnostico"
        String  estado,
        LocalDate fechaInicio,
        LocalDate fechaFin
) {}