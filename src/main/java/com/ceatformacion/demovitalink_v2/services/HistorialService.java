package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.EventoHistorialDTO;
import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.Sintomas;
import com.ceatformacion.demovitalink_v2.model.Tratamientos;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.SintomasRepository;
import com.ceatformacion.demovitalink_v2.repository.TratamientosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class HistorialService {

    private final SintomasRepository sintomasRepository;
    private final TratamientosRepository tratamientosRepository;
    private final CitasRepository citasRepository;

    public HistorialService(SintomasRepository sintomasRepository,
                            TratamientosRepository tratamientosRepository,
                            CitasRepository citasRepository) {
        this.sintomasRepository = sintomasRepository;
        this.tratamientosRepository = tratamientosRepository;
        this.citasRepository = citasRepository;
    }

    /**
     * Obtiene el historial completo de un usuario con filtros opcionales.
     *
     * @param userId            ID del usuario/paciente
     * @param tipo              Filtro por tipo: SINTOMA, TRATAMIENTO, CITA (null = todos)
     * @param desde             Fecha inicio (null = sin límite)
     * @param hasta             Fecha fin (null = sin límite)
     * @param zona              Filtro por zona corporal (solo aplica a síntomas)
     * @param estadoTratamiento Filtro por estado de tratamiento
     * @param pageable          Configuración de paginación
     * @return Page con los eventos del historial
     */
    public Page<EventoHistorialDTO> obtenerHistorial(Integer userId,
                                                     String tipo,
                                                     LocalDate desde,
                                                     LocalDate hasta,
                                                     String zona,
                                                     String estadoTratamiento,
                                                     Pageable pageable) {

        List<EventoHistorialDTO> todosEventos = new ArrayList<>();

        // Convertir fechas a LocalDateTime para comparaciones
        LocalDateTime desdeDateTime = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaDateTime = hasta != null ? hasta.atTime(23, 59, 59) : null;

        // === SÍNTOMAS ===
        if (tipo == null || tipo.isEmpty() || "SINTOMA".equalsIgnoreCase(tipo)) {
            List<Sintomas> sintomas = sintomasRepository.findSintomasByUsuario_Id_usuario(userId);

            for (Sintomas s : sintomas) {
                // Filtro por fechas
                if (desdeDateTime != null && s.getFechaRegistro().isBefore(desdeDateTime)) continue;
                if (hastaDateTime != null && s.getFechaRegistro().isAfter(hastaDateTime)) continue;

                // Filtro por zona
                if (zona != null && !zona.isEmpty()) {
                    if (s.getZona() == null || !s.getZona().name().equalsIgnoreCase(zona)) continue;
                }

                todosEventos.add(EventoHistorialDTO.fromSintoma(
                        s.getId_sintoma(),
                        s.getFechaRegistro(),
                        s.getTipo() != null ? s.getTipo().name() : null,
                        s.getDescripcion(),
                        s.getZona() != null ? s.getZona().name() : null
                ));
            }
        }

        // === TRATAMIENTOS ===
        if (tipo == null || tipo.isEmpty() || "TRATAMIENTO".equalsIgnoreCase(tipo)) {
            List<Tratamientos> tratamientos = tratamientosRepository.findByUsuarioId(userId);

            for (Tratamientos t : tratamientos) {
                LocalDateTime fechaTratamiento = t.getFecha_inicio() != null
                        ? t.getFecha_inicio().atStartOfDay()
                        : LocalDateTime.now();

                // Filtro por fechas
                if (desdeDateTime != null && fechaTratamiento.isBefore(desdeDateTime)) continue;
                if (hastaDateTime != null && fechaTratamiento.isAfter(hastaDateTime)) continue;

                // Filtro por estado
                if (estadoTratamiento != null && !estadoTratamiento.isEmpty()) {
                    if (t.getEstado_tratamiento() == null ||
                            !t.getEstado_tratamiento().equalsIgnoreCase(estadoTratamiento)) continue;
                }

                todosEventos.add(EventoHistorialDTO.fromTratamiento(
                        t.getId_tratamiento(),
                        fechaTratamiento,
                        t.getNombre_tratamiento(),
                        t.getObservaciones(),
                        t.getEstado_tratamiento()
                ));
            }
        }

        // === CITAS ===
        if (tipo == null || tipo.isEmpty() || "CITA".equalsIgnoreCase(tipo)) {
            List<Citas> citas = citasRepository.findCitasByUsuario(
                    new com.ceatformacion.demovitalink_v2.model.Usuarios() {{
                        setId_usuario(userId);
                    }}
            );

            // Alternativa más segura: crear query en el repositorio
            // List<Citas> citas = citasRepository.findByUsuarioId(userId);

            for (Citas c : citas) {
                LocalDateTime fechaCita = c.getFecha() != null
                        ? c.getFecha().atTime(c.getHora() != null ? c.getHora() : java.time.LocalTime.MIDNIGHT)
                        : LocalDateTime.now();

                // Filtro por fechas
                if (desdeDateTime != null && fechaCita.isBefore(desdeDateTime)) continue;
                if (hastaDateTime != null && fechaCita.isAfter(hastaDateTime)) continue;

                todosEventos.add(EventoHistorialDTO.fromCita(
                        c.getId_cita(),
                        fechaCita,
                        c.getTitulo(),
                        c.getDescripcion(),
                        c.getEstado() != null ? c.getEstado().name() : null
                ));
            }
        }

        // Ordenar por fecha descendente (más recientes primero)
        todosEventos.sort(Comparator.comparing(EventoHistorialDTO::getFecha).reversed());

        // Aplicar paginación manual
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), todosEventos.size());

        List<EventoHistorialDTO> paginado = start < todosEventos.size()
                ? todosEventos.subList(start, end)
                : new ArrayList<>();

        return new PageImpl<>(paginado, pageable, todosEventos.size());
    }

    /**
     * Obtiene estadísticas resumidas del historial de un usuario.
     */
    public HistorialStats obtenerEstadisticas(Integer userId) {
        long totalSintomas = sintomasRepository.countByUsuario(userId);
        long totalTratamientos = tratamientosRepository.countByUsuario(userId);
        long totalCitas = citasRepository.countByUsuario(userId);

        return new HistorialStats(totalSintomas, totalTratamientos, totalCitas);
    }

    /**
     * Clase interna para estadísticas del historial.
     */
    public static class HistorialStats {
        private final long totalSintomas;
        private final long totalTratamientos;
        private final long totalCitas;
        private final long total;

        public HistorialStats(long totalSintomas, long totalTratamientos, long totalCitas) {
            this.totalSintomas = totalSintomas;
            this.totalTratamientos = totalTratamientos;
            this.totalCitas = totalCitas;
            this.total = totalSintomas + totalTratamientos + totalCitas;
        }

        public long getTotalSintomas() { return totalSintomas; }
        public long getTotalTratamientos() { return totalTratamientos; }
        public long getTotalCitas() { return totalCitas; }
        public long getTotal() { return total; }
    }
}