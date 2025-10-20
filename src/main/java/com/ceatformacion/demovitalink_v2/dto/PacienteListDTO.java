package com.ceatformacion.demovitalink_v2.dto;

public record PacienteListDTO(
        Integer idCliente,
        String nombre,
        String apellidos,
        Integer medicoId,
        String medicoUsername
) {}