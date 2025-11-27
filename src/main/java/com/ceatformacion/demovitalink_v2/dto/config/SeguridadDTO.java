package com.ceatformacion.demovitalink_v2.dto.config;

public record SeguridadDTO(
        String nuevaPassword,      // Solo para enviar, nunca se devuelve
        String confirmarPassword,  // Solo para validación
        Boolean activar2FA         // Flag de 2FA
) {}