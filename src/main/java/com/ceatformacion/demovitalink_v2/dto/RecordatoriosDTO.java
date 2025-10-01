package com.ceatformacion.demovitalink_v2.dto;

import com.ceatformacion.demovitalink_v2.model.Canal;
import com.ceatformacion.demovitalink_v2.model.Repeticion;
import com.ceatformacion.demovitalink_v2.model.TipoRecordatorio;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record RecordatoriosDTO (
    int id_recordatorio,
    @NotNull
    int id_usuario,
    @NotBlank @Size(max = 200) String titulo,
    @NotNull TipoRecordatorio tipo,
    @NotNull LocalDateTime fechaHora,
    @NotNull Repeticion repeticion,
    @NotNull Canal canal,
    String vinculoTipo,                // "TRATAMIENTO" | "CITA" | null
    Integer vinculoId,                  // id vinculado | null
    String descripcion,
    boolean completado
) {}