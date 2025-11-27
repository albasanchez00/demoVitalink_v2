package com.ceatformacion.demovitalink_v2.dto.config;

public record PrivacidadDTO(
        String visibilidad,   // PUBLICO | LIMITADO | PRIVADO
        Boolean usoDatos,     // Permitir uso de datos con fines estadísticos
        Boolean boletines     // Aceptar recibir boletines
) {}