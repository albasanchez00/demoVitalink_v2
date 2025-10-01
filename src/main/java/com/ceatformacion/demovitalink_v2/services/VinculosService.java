package com.ceatformacion.demovitalink_v2.services;

import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class VinculosService {
    // TODO: Inyecta repos de Citas/Tratamientos y filtra por usuario + estado=ACTIVO
    public Map<String, Object> obtenerVinculosActivos(int id_usuario){
        Map<String, Object> res = new LinkedHashMap<>();
        res.put("TRATAMIENTOS", List.of(
                Map.of("id","T-100", "nombre","Amoxicilina 500mg — 7 días (cada 8h)"),
                Map.of("id","T-101", "nombre","Fisioterapia rodilla — 4 semanas (2/sem)")
        ));
        res.put("CITAS", List.of(
                Map.of("id","C-220", "nombre","Cita con Dra. López — 28/09 10:30"),
                Map.of("id","C-221", "nombre","Extracción de sangre — 02/10 08:15")
        ));
        return res;
    }
}

