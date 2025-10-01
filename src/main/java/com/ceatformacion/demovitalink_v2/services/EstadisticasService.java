package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.CategoryValueDTO;
import com.ceatformacion.demovitalink_v2.dto.EstadisticasResponse;
import com.ceatformacion.demovitalink_v2.dto.TimePointDTO;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.SintomasRepository;
import com.ceatformacion.demovitalink_v2.repository.TratamientosRepository;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Service
public class EstadisticasService {

    private final SintomasRepository sintomasRepository;
    private final CitasRepository citasRepository;
    private final TratamientosRepository tratamientosRepository;

    public EstadisticasService(
            SintomasRepository sintomasRepository,
            CitasRepository citasRepository,
            TratamientosRepository tratamientosRepository
    ) {
        this.sintomasRepository = sintomasRepository;
        this.citasRepository = citasRepository;
        this.tratamientosRepository = tratamientosRepository;
    }

    public EstadisticasResponse calcular(int id_usuario, LocalDate desde, LocalDate hasta, String tipoSintoma) {
        final String tipo = (tipoSintoma == null || tipoSintoma.isBlank()) ? "todos" : tipoSintoma;

        // === Síntomas ===
        List<TimePointDTO> sintomasPorDia = sintomasRepository
                .contarPorDia(id_usuario, desde, hasta, tipo)
                .stream()
                .map(r -> new TimePointDTO(toLocalDate(r[0]), ((Number) r[1]).longValue()))
                .toList();

        List<CategoryValueDTO> sintomasPorTipo = sintomasRepository
                .contarPorTipo(id_usuario, desde, hasta)
                .stream()
                .map(r -> new CategoryValueDTO((String) r[0], ((Number) r[1]).longValue()))
                .toList();

        // === Citas ===
        List<TimePointDTO> citasPorDia = citasRepository
                .contarPorDia(id_usuario, desde, hasta)
                .stream()
                .map(r -> new TimePointDTO(toLocalDate(r[0]), ((Number) r[1]).longValue()))
                .toList();

        List<CategoryValueDTO> citasProximasVsPasadas = citasRepository
                .contarProximasVsPasadas(id_usuario, desde, hasta)
                .stream()
                .map(r -> new CategoryValueDTO((String) r[0], ((Number) r[1]).longValue()))
                .toList();

        // === Tratamientos === (agrupado por estado_tratamiento)
        List<CategoryValueDTO> tratamientosPorTipo = tratamientosRepository
                .contarPorEstado(id_usuario)
                .stream()
                .map(r -> new CategoryValueDTO((String) r[0], ((Number) r[1]).longValue()))
                .toList();

        return new EstadisticasResponse(
                sintomasPorDia,
                sintomasPorTipo,
                citasPorDia,
                citasProximasVsPasadas,
                tratamientosPorTipo
        );
    }

    // Conversión robusta: soporta LocalDate, java.sql.Date, Timestamp y java.util.Date
    private static LocalDate toLocalDate(Object o) {
        if (o == null) return null;

        if (o instanceof LocalDate ld) {
            return ld;
        }
        if (o instanceof java.sql.Date sd) {
            return sd.toLocalDate();
        }
        if (o instanceof java.sql.Timestamp ts) {
            return ts.toLocalDateTime().toLocalDate();
        }
        if (o instanceof java.util.Date ud) {
            return ud.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        }

        throw new IllegalArgumentException("No se puede convertir a LocalDate desde: " + o.getClass());
    }
}

