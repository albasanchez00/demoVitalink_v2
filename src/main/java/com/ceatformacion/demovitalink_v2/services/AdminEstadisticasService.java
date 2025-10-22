package com.ceatformacion.demovitalink_v2.services;


import com.ceatformacion.demovitalink_v2.dto.admin.*;
import org.springframework.data.domain.Page;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

@Service
public class AdminEstadisticasService {

    private final JdbcTemplate jdbc;

    public AdminEstadisticasService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    private enum GroupBy { day, week, month;
        static GroupBy of(String v) {
            try { return GroupBy.valueOf(v == null ? "day" : v.toLowerCase()); }
            catch (Exception e) { return day; }
        }
    }

    /* ================= KPI Overview ================= */
    public OverviewStatsDTO overview(LocalDate from, LocalDate to, Long medicoId) {
        long totalPacientes = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE rol = 'USER'", Long.class);

        long totalMedicos = jdbc.queryForObject(
                "SELECT COUNT(*) FROM usuarios WHERE rol = 'MEDICO'", Long.class);

        long tratamientosActivos = jdbc.queryForObject(
                "SELECT COUNT(*) FROM tratamientos WHERE estado_tratamiento = 'ACTIVO' OR fecha_fin IS NULL", Long.class);

        long citasTotales = jdbc.queryForObject(
                "SELECT COUNT(*) FROM citas WHERE fecha BETWEEN ? AND ?",
                Long.class, Date.valueOf(from), Date.valueOf(to));

        long citasCompletadas = jdbc.queryForObject(
                "SELECT COUNT(*) FROM citas WHERE estado = 'ATENDIDA' AND fecha BETWEEN ? AND ?",
                Long.class, Date.valueOf(from), Date.valueOf(to));

        double adherencia = (citasTotales == 0) ? 0 : (citasCompletadas * 100.0 / citasTotales);

        long citasSemana = jdbc.queryForObject(
                "SELECT COUNT(*) FROM citas WHERE YEARWEEK(fecha, 1) = YEARWEEK(CURDATE(), 1)",
                Long.class);

        Double esperaMedia = jdbc.queryForObject(
                """
                SELECT AVG(DATEDIFF(fecha, CURDATE()))
                FROM citas
                WHERE fecha BETWEEN ? AND ?
                """,
                Double.class, Date.valueOf(from), Date.valueOf(to)
        );

        return new OverviewStatsDTO(
                totalPacientes, totalMedicos, tratamientosActivos,
                Math.round(adherencia * 100.0) / 100.0,
                citasSemana, esperaMedia
        );
    }

    /* ================= Serie de Adherencia (proxy: citas atendidas / total) ================= */
    public SerieDTO serieAdherencia(LocalDate from, LocalDate to, String group) {
        GroupBy gb = GroupBy.of(group);
        String dateExpr = switch (gb) {
            case day  -> "DATE(fecha)";
            case week -> "STR_TO_DATE(CONCAT(YEAR(fecha),'-',LPAD(WEEK(fecha,1),2,'0'),' Monday'), '%X-%V %W')";
            case month-> "DATE_FORMAT(fecha, '%Y-%m-01')";
        };

        List<String> labels = new ArrayList<>();
        List<Number> values = new ArrayList<>();

        jdbc.query(
                ("""
                SELECT %s AS periodo,
                       SUM(estado='ATENDIDA') / COUNT(*) * 100 AS pct
                FROM citas
                WHERE fecha BETWEEN ? AND ?
                GROUP BY periodo
                ORDER BY periodo
                """).formatted(dateExpr),
                (RowCallbackHandler) rs -> {
                    labels.add(rs.getDate("periodo").toLocalDate().toString());
                    values.add(Math.round(rs.getDouble("pct") * 10.0) / 10.0);
                },
                Date.valueOf(from), Date.valueOf(to)
        );

        return new SerieDTO(labels, values);
    }

    /* ================= Serie de Citas ================= */
    public CitasSerieDTO serieCitas(LocalDate from, LocalDate to, String group, Long medicoId) {
        GroupBy gb = GroupBy.of(group);
        String dateExpr = switch (gb) {
            case day  -> "DATE(fecha)";
            case week -> "STR_TO_DATE(CONCAT(YEAR(fecha),'-',LPAD(WEEK(fecha,1),2,'0'),' Monday'), '%X-%V %W')";
            case month-> "DATE_FORMAT(fecha, '%Y-%m-01')";
        };

        String filtroMedico = (medicoId == null ? "" : " AND id_medico = " + medicoId + " ");

        List<String> labels = new ArrayList<>();
        List<Long> created   = new ArrayList<>();
        List<Long> attended  = new ArrayList<>();
        List<Long> cancelled = new ArrayList<>();

        jdbc.query(
                ("""
            SELECT %s AS periodo,
                   -- created: reservas/creadas en tu modelo = CONFIRMADA + PENDIENTE
                   SUM(estado IN ('CONFIRMADA','PENDIENTE')) AS created,
                   SUM(estado = 'ATENDIDA')                 AS attended,
                   SUM(estado = 'CANCELADA')                AS cancelled
            FROM citas
            WHERE fecha BETWEEN ? AND ? %s
            GROUP BY periodo
            ORDER BY periodo
            """).formatted(dateExpr, filtroMedico),
                rs -> {
                    labels.add(rs.getDate("periodo").toLocalDate().toString());
                    created.add(rs.getLong("created"));
                    attended.add(rs.getLong("attended"));
                    cancelled.add(rs.getLong("cancelled"));
                },
                Date.valueOf(from), Date.valueOf(to)
        );

        return new CitasSerieDTO(labels, created, attended, cancelled);
    }


    /* ================= Top Síntomas ================= */
    public List<TopSintomaDTO> topSintomas(LocalDate from, LocalDate to, int limit) {
        return jdbc.query(
                """
                SELECT tipo AS nombre, COUNT(*) AS total
                FROM sintomas
                WHERE fecha_registro BETWEEN ? AND ?
                GROUP BY tipo
                ORDER BY total DESC
                LIMIT ?
                """,
                (rs, i) -> new TopSintomaDTO(rs.getString("nombre"), rs.getLong("total")),
                Date.valueOf(from), Date.valueOf(to), limit
        );
    }

    /* ================= Serie Tratamientos ================= */
    public Map<String,Object> serieTratamientos(LocalDate from, LocalDate to, String group) {
        GroupBy gb = GroupBy.of(group);
        String dateExprIni = switch (gb) {
            case day  -> "DATE(fecha_inicio)";
            case week -> "STR_TO_DATE(CONCAT(YEAR(fecha_inicio),'-',LPAD(WEEK(fecha_inicio,1),2,'0'),' Monday'), '%X-%V %W')";
            case month-> "DATE_FORMAT(fecha_inicio, '%Y-%m-01')";
        };
        String dateExprFin = switch (gb) {
            case day  -> "DATE(fecha_fin)";
            case week -> "STR_TO_DATE(CONCAT(YEAR(fecha_fin),'-',LPAD(WEEK(fecha_fin,1),2,'0'),' Monday'), '%X-%V %W')";
            case month-> "DATE_FORMAT(fecha_fin, '%Y-%m-01')";
        };

        Map<String, Long> mapStart = new LinkedHashMap<>();
        Map<String, Long> mapFin = new LinkedHashMap<>();

        jdbc.query(
                ("""
                SELECT %s AS periodo, COUNT(*) AS n
                FROM tratamientos
                WHERE fecha_inicio BETWEEN ? AND ?
                GROUP BY periodo ORDER BY periodo
                """).formatted(dateExprIni),
                (RowCallbackHandler) rs -> mapStart.put(rs.getDate("periodo").toLocalDate().toString(), rs.getLong("n")),
                Date.valueOf(from), Date.valueOf(to)
        );

        jdbc.query(
                ("""
                SELECT %s AS periodo, COUNT(*) AS n
                FROM tratamientos
                WHERE fecha_fin BETWEEN ? AND ?
                GROUP BY periodo ORDER BY periodo
                """).formatted(dateExprFin),
                (RowCallbackHandler) rs -> mapFin.put(rs.getDate("periodo").toLocalDate().toString(), rs.getLong("n")),
                Date.valueOf(from), Date.valueOf(to)
        );

        TreeSet<String> labels = new TreeSet<>(mapStart.keySet());
        labels.addAll(mapFin.keySet());
        List<Long> started = new ArrayList<>();
        List<Long> finished = new ArrayList<>();

        for (String k : labels) {
            started.add(mapStart.getOrDefault(k, 0L));
            finished.add(mapFin.getOrDefault(k, 0L));
        }

        Map<String,Object> body = new HashMap<>();
        body.put("labels", new ArrayList<>(labels));
        body.put("started", started);
        body.put("finished", finished);
        return body;
    }

    /* ================= Distribución por especialidad ================= */
    public List<DistribEspecialidadDTO> distribEspecialidades(LocalDate from, LocalDate to) {
        return jdbc.query(
                """
                SELECT especialidad AS especialidad, COUNT(*) AS total
                FROM config_medico
                GROUP BY especialidad
                ORDER BY total DESC
                """,
                (rs, i) -> new DistribEspecialidadDTO(rs.getString("especialidad"), rs.getLong("total"))
        );
    }

    /* ================= Serie de Usuarios ================= */
    public Map<String,Object> serieUsuarios(LocalDate from, LocalDate to, String group) {
        GroupBy gb = GroupBy.of(group);
        String dateExpr = "DATE(id_usuario)"; // no hay creado_en → proxy

        Map<String, Long> pacientes = new LinkedHashMap<>();
        Map<String, Long> medicos = new LinkedHashMap<>();

        jdbc.query(
                """
                SELECT rol, COUNT(*) AS n
                FROM usuarios
                WHERE rol IN ('USER','MEDICO')
                GROUP BY rol
                """,
                (RowCallbackHandler) rs -> {
                    if ("USER".equals(rs.getString("rol"))) pacientes.put(LocalDate.now().toString(), rs.getLong("n"));
                    if ("MEDICO".equals(rs.getString("rol"))) medicos.put(LocalDate.now().toString(), rs.getLong("n"));
                }
        );

        Map<String,Object> body = new HashMap<>();
        body.put("labels", List.of(LocalDate.now().toString()));
        body.put("pacientes", List.of(pacientes.values().stream().findFirst().orElse(0L)));
        body.put("medicos", List.of(medicos.values().stream().findFirst().orElse(0L)));
        return body;
    }

    /* ================= Reporte de Citas ================= */
    public Page<Map<String,Object>> reporteCitas(LocalDate from, LocalDate to, Long medicoId, int page, int size, String sort) {
        String filtroMed = (medicoId == null ? "" : " AND c.id_medico = " + medicoId + " ");
        String orderBy = " ORDER BY " + (sort == null || sort.isBlank()
                ? "c.fecha DESC"
                : (sort.contains("desc")) ? "c.fecha DESC" : "c.fecha ASC");
        int offset = page * size;

        List<Map<String,Object>> rows = jdbc.query(
                ("""
                SELECT c.id_cita, DATE(c.fecha) AS fecha, c.estado, c.titulo, 
                       u.username AS pacienteNombre, m.username AS medicoNombre
                FROM citas c
                JOIN usuarios u ON u.id_usuario = c.id_usuario
                JOIN usuarios m ON m.id_usuario = c.id_medico
                WHERE c.fecha BETWEEN ? AND ? %s
                %s
                LIMIT ? OFFSET ?
                """).formatted(filtroMed, orderBy),
                (rs, i) -> {
                    Map<String,Object> map = new HashMap<>();
                    map.put("id", rs.getLong("id_cita"));
                    map.put("fecha", rs.getDate("fecha").toLocalDate());
                    map.put("estado", rs.getString("estado"));
                    map.put("titulo", rs.getString("titulo"));
                    map.put("pacienteNombre", rs.getString("pacienteNombre"));
                    map.put("medicoNombre", rs.getString("medicoNombre"));
                    return map;
                },
                Date.valueOf(from), Date.valueOf(to), size, offset
        );

        Long total = jdbc.queryForObject(
                ("SELECT COUNT(*) FROM citas c WHERE c.fecha BETWEEN ? AND ? " + filtroMed),
                Long.class, Date.valueOf(from), Date.valueOf(to)
        );

        return new org.springframework.data.domain.PageImpl<>(rows, org.springframework.data.domain.PageRequest.of(page, size), total);
    }

    /* ================= REPORTE: TRATAMIENTOS ================= */
    public Page<Map<String,Object>> reporteTratamientos(LocalDate from, LocalDate to, int page, int size, String sort) {
        String orderBy = " ORDER BY " + (sort == null || sort.isBlank()
                ? "t.fecha_inicio DESC"
                : (sort.contains("desc") ? "t.fecha_inicio DESC" : "t.fecha_inicio ASC"));
        int offset = page * size;

        List<Map<String,Object>> rows = jdbc.query(
                """
                SELECT t.id_tratamiento, t.fecha_inicio, t.fecha_fin, t.estado_tratamiento,
                       u.username AS paciente, 
                       -- médico: tomamos el último médico que atendió al paciente en el rango (si lo hubo)
                       (SELECT m.username 
                        FROM citas c 
                        JOIN usuarios m ON m.id_usuario = c.id_medico
                        WHERE c.id_usuario = t.id_usuario AND c.fecha BETWEEN ? AND ?
                        ORDER BY c.fecha DESC LIMIT 1) AS medico
                FROM tratamientos t
                JOIN usuarios u ON u.id_usuario = t.id_usuario
                WHERE (t.fecha_inicio BETWEEN ? AND ? 
                       OR (t.fecha_fin IS NOT NULL AND t.fecha_fin BETWEEN ? AND ?))
                """ + orderBy + " LIMIT ? OFFSET ?",
                (rs,i)->{
                    Map<String,Object> m = new HashMap<>();
                    m.put("id", rs.getLong("id_tratamiento"));
                    m.put("inicio", rs.getDate("fecha_inicio"));
                    m.put("fin", rs.getDate("fecha_fin"));
                    m.put("paciente", rs.getString("paciente"));
                    m.put("medico", rs.getString("medico"));
                    m.put("estado", rs.getString("estado_tratamiento"));
                    return m;
                },
                Date.valueOf(from), Date.valueOf(to),
                Date.valueOf(from), Date.valueOf(to), Date.valueOf(from), Date.valueOf(to),
                size, offset
        );

        Long total = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM tratamientos t
                WHERE (t.fecha_inicio BETWEEN ? AND ? 
                       OR (t.fecha_fin IS NOT NULL AND t.fecha_fin BETWEEN ? AND ?))
                """,
                Long.class, Date.valueOf(from), Date.valueOf(to), Date.valueOf(from), Date.valueOf(to)
        );

        return new org.springframework.data.domain.PageImpl<>(rows, org.springframework.data.domain.PageRequest.of(page,size), total);
    }


    /* ================= REPORTE: ADHERENCIA (proxy por paciente) ================= */
    public Page<Map<String,Object>> reporteAdherencia(LocalDate from, LocalDate to, int page, int size, String sort) {
        String orderBy = " ORDER BY " + (sort == null || sort.isBlank()
                ? "pct DESC" : (sort.contains("asc") ? "pct ASC" : "pct DESC"));
        int offset = page * size;

        // Nota: proxy: % atendidas/total por paciente en el rango
        List<Map<String,Object>> rows = jdbc.query(
                """
                SELECT u.id_usuario, u.username AS paciente,
                       -- nombre del tratamiento más reciente activo/en rango (si existe)
                       (SELECT tt.nombre_tratamiento 
                        FROM tratamientos tt
                        WHERE tt.id_usuario = u.id_usuario 
                          AND (tt.fecha_inicio <= ? AND (tt.fecha_fin IS NULL OR tt.fecha_fin >= ?))
                        ORDER BY tt.fecha_inicio DESC LIMIT 1) AS tratamiento,
                       SUM(c.estado = 'ATENDIDA') / NULLIF(COUNT(*),0) * 100 AS pct
                FROM usuarios u
                JOIN citas c ON c.id_usuario = u.id_usuario
                WHERE u.rol = 'USER'
                  AND c.fecha BETWEEN ? AND ?
                GROUP BY u.id_usuario, u.username
                """ + orderBy + " LIMIT ? OFFSET ?",
                (rs,i)->{
                    Map<String,Object> m = new HashMap<>();
                    m.put("paciente", rs.getString("paciente"));
                    m.put("tratamiento", rs.getString("tratamiento"));
                    m.put("adherencia", rs.getDouble("pct"));      // %
                    m.put("omisiones", null); // no tenemos dosis omitidas -> ‘—’
                    return m;
                },
                Date.valueOf(to), Date.valueOf(from),
                Date.valueOf(from), Date.valueOf(to),
                size, offset
        );

        Long total = jdbc.queryForObject(
                """
                SELECT COUNT(DISTINCT u.id_usuario)
                FROM usuarios u
                JOIN citas c ON c.id_usuario = u.id_usuario
                WHERE u.rol = 'USER' AND c.fecha BETWEEN ? AND ?
                """,
                Long.class, Date.valueOf(from), Date.valueOf(to)
        );

        return new org.springframework.data.domain.PageImpl<>(rows, org.springframework.data.domain.PageRequest.of(page,size), total);
    }

    // Reporte: Síntomas (paginado)
    public Page<Map<String, Object>> reporteSintomas(
            LocalDate from, LocalDate to, int page, int size, String sort) {

        String orderBy = " ORDER BY " + (
                (sort == null || sort.isBlank())
                        ? "s.fecha_registro DESC"
                        : (sort.toLowerCase().contains("asc") ? "s.fecha_registro ASC" : "s.fecha_registro DESC")
        );

        int offset = page * size;

        List<Map<String, Object>> rows = jdbc.query(
                """
                SELECT s.id_sintoma,
                       s.fecha_registro,
                       s.tipo,
                       s.zona,
                       u.username AS paciente
                FROM sintomas s
                JOIN usuarios u ON u.id_usuario = s.id_usuario
                WHERE s.fecha_registro BETWEEN ? AND ?
                """ + orderBy + " LIMIT ? OFFSET ?",
                (rs, i) -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("id", rs.getLong("id_sintoma"));
                    // fecha_registro es datetime(6) -> LocalDateTime
                    var ts = rs.getTimestamp("fecha_registro");
                    m.put("fecha", ts != null ? ts.toLocalDateTime() : null);
                    m.put("paciente", rs.getString("paciente"));
                    m.put("tipo", rs.getString("tipo"));
                    m.put("zona", rs.getString("zona"));
                    // No hay columna 'severidad' en tu BD actual
                    m.put("severidad", null);
                    return m;
                },
                java.sql.Timestamp.valueOf(from.atStartOfDay()),
                java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay().minusNanos(1)),
                size, offset
        );

        Long total = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM sintomas s
                WHERE s.fecha_registro BETWEEN ? AND ?
                """,
                Long.class,
                java.sql.Timestamp.valueOf(from.atStartOfDay()),
                java.sql.Timestamp.valueOf(to.plusDays(1).atStartOfDay().minusNanos(1))
        );

        return new org.springframework.data.domain.PageImpl<>(
                rows,
                org.springframework.data.domain.PageRequest.of(page, size),
                total
        );
    }

}