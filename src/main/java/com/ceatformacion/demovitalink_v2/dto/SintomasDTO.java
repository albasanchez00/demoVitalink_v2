// src/main/java/com/ceatformacion/demovitalink_v2/dto/SintomaDTO.java
package com.ceatformacion.demovitalink_v2.dto;

import com.ceatformacion.demovitalink_v2.model.Sintomas;
import java.time.LocalDateTime;

public record SintomasDTO(
        int id_sintoma,
        String tipo,
        String zona,
        String descripcion,
        LocalDateTime fechaRegistro
) {
    public static SintomasDTO from(Sintomas s) {
        return new SintomasDTO(
                s.getId_sintoma(),
                s.getTipo(),
                s.getZona(),         // <-- asegúrate de tener getZona() en la entidad
                s.getDescripcion(),
                s.getFechaRegistro()
        );
    }
}
