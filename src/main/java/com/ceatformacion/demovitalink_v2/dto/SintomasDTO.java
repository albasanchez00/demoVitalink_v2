// src/main/java/com/ceatformacion/demovitalink_v2/dto/SintomaDTO.java
package com.ceatformacion.demovitalink_v2.dto;

import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.model.TipoSintoma;
import com.ceatformacion.demovitalink_v2.model.ZonaCorporal;

import java.time.LocalDateTime;

public record SintomasDTO(
        int id_sintoma,
        Integer usuarioId,
        TipoSintoma tipo,
        ZonaCorporal zona,
        String descripcion,
        LocalDateTime fechaRegistro
) {
    public static SintomasDTO from(Sintomas s) {
        return new SintomasDTO(
                s.getId_sintoma(),
                s.getUsuario() != null ? s.getUsuario().getId_usuario() : null,
                s.getTipo(),
                s.getZona(),
                s.getDescripcion(),
                s.getFechaRegistro()
        );
    }
}
