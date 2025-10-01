package com.ceatformacion.demovitalink_v2.mapper;

import com.ceatformacion.demovitalink_v2.dto.RecordatoriosDTO;
import com.ceatformacion.demovitalink_v2.model.Recordatorios;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import org.springframework.stereotype.Component;

@Component
public class RecordatoriosMapper {
    public Recordatorios toEntity(RecordatoriosDTO d, Usuarios u){
        Recordatorios r = new Recordatorios();
        // Solo para update; en altas déjalo en 0
        if (d.id_recordatorio() > 0) {
            r.setId_recordatorio(d.id_recordatorio());
        }
        r.setUsuario(u);
        r.setTitulo(d.titulo());
        r.setTipo(d.tipo());
        r.setFechaHora(d.fechaHora());
        r.setRepeticion(d.repeticion());
        r.setCanal(d.canal());
        r.setVinculoTipo(d.vinculoTipo());
        r.setVinculoId(d.vinculoId());
        r.setDescripcion(d.descripcion());
        r.setCompletado(d.completado());
        return r;
    }

    public RecordatoriosDTO toDTO(Recordatorios r){
        return new RecordatoriosDTO(
                r.getId_recordatorio(),
                r.getUsuario().getId_usuario(),
                r.getTitulo(),
                r.getTipo(),
                r.getFechaHora(),
                r.getRepeticion(),
                r.getCanal(),
                r.getVinculoTipo(),
                r.getVinculoId(),
                r.getDescripcion(),
                r.isCompletado()
        );
    }
}
