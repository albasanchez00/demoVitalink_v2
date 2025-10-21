package com.ceatformacion.demovitalink_v2.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public record CitaAdminDTO(
        Integer id,
        Integer pacienteId,
        String  pacienteNombre,
        Integer medicoId,
        String  medicoNombre,
        String  titulo,
        String  estado,
        LocalDate fecha,
        LocalTime hora
) {}