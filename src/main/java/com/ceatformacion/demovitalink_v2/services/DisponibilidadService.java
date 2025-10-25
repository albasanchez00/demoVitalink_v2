package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.ConfigAdminDTO;
import com.ceatformacion.demovitalink_v2.model.*;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.ConfigAgendaRepository;
import com.ceatformacion.demovitalink_v2.repository.ConfigMedicoRepository;
import com.ceatformacion.demovitalink_v2.repository.DisponibilidadMedicaRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
/**
 * Genera slots disponibles (HH:mm) para un médico/fecha basándose en:
 * - Configuración Global: citaDuracionMin, horarioInicio/Fin, diasNoLaborables
 * - Citas existentes del médico en la fecha (bloquea todo salvo CANCELADA)
 * - Si la fecha es hoy: oculta slots ya transcurridos
 */
@Service
public class DisponibilidadService {

    private static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    private final CitasRepository citasRepo;
    private final ConfigAgendaRepository configAgendaRepo;
    private final ConfigAdminService configAdminService;
    private final ObjectMapper mapper;

    private static final Map<DayOfWeek, String> DAY_KEY = Map.of(
            DayOfWeek.MONDAY, "lunes",
            DayOfWeek.TUESDAY, "martes",
            DayOfWeek.WEDNESDAY, "miercoles",
            DayOfWeek.THURSDAY, "jueves",
            DayOfWeek.FRIDAY, "viernes",
            DayOfWeek.SATURDAY, "sabado",
            DayOfWeek.SUNDAY, "domingo"
    );

    public DisponibilidadService(CitasRepository citasRepo,
                                 ConfigAgendaRepository configAgendaRepo,
                                 ConfigAdminService configAdminService,
                                 ObjectMapper mapper) {
        this.citasRepo = citasRepo;
        this.configAgendaRepo = configAgendaRepo;
        this.configAdminService = configAdminService;
        this.mapper = mapper;
    }

    /** Devuelve horas disponibles en formato HH:mm para un médico en una fecha. */
    public List<String> calcularHorasDisponibles(int idMedico, LocalDate fecha, Integer slotMinutos) {
        if (idMedico <= 0 || fecha == null) return List.of();

        // --- Configuración Global ---
        ConfigAdminDTO cfg = configAdminService.getActual();
        int slot = (slotMinutos != null && slotMinutos > 0)
                ? slotMinutos
                : (cfg.citaDuracionMin() != null ? cfg.citaDuracionMin() : 30);

        LocalTime hInicioGlobal = parseOrDefault(cfg.horarioInicio(), LocalTime.of(8, 0));
        LocalTime hFinGlobal = parseOrDefault(cfg.horarioFin(), LocalTime.of(17, 0));
        if (!hInicioGlobal.isBefore(hFinGlobal)) return List.of();

        // Días no laborables globales
        if (cfg.diasNoLaborables() != null && cfg.diasNoLaborables().contains(fecha.toString())) {
            return List.of();
        }

        // --- Tramos del médico según ConfigAgenda ---
        List<Tramo> tramosMedico = readTramosMedicoParaDia(idMedico, fecha.getDayOfWeek());

        List<Tramo> tramos = tramosMedico.isEmpty()
                ? List.of(new Tramo(hInicioGlobal, hFinGlobal))
                : tramosMedico;

        // Generar slots teóricos
        List<Slot> teoricos = generarSlots(tramos, slot);

        // Ocultar horas pasadas si es hoy
        if (fecha.isEqual(LocalDate.now())) {
            LocalTime ahora = LocalTime.now().withSecond(0).withNano(0);
            teoricos = teoricos.stream().filter(s -> s.inicio.isAfter(ahora)).toList();
        }
        if (teoricos.isEmpty()) return List.of();

        // Citas del médico ese día (ocupan huecos)
        List<Citas> citas = citasRepo.findByMedicoAndFecha(idMedico, fecha);
        List<Slot> ocupados = citas.stream()
                .filter(this::bloquea)
                .map(this::toSlot)
                .collect(Collectors.toList());

        // Devolver los huecos libres
        return teoricos.stream()
                .filter(s -> ocupados.stream().noneMatch(o -> solapan(s, o)))
                .map(s -> s.inicio.format(HH_MM))
                .toList();
    }

    /** Valida si una hora concreta está libre (usado por CitasController). */
    public boolean esValida(int idMedico, LocalDate fecha, LocalTime hora, Integer durMin) {
        List<String> horas = calcularHorasDisponibles(idMedico, fecha, durMin);
        return horas.contains(hora.format(HH_MM));
    }

    /* ================== Helpers ================== */

    /** Lee y parsea los tramos de ConfigAgenda.disponibilidadJson para el día indicado. */
    private List<Tramo> readTramosMedicoParaDia(int idMedico, DayOfWeek dow) {
        return configAgendaRepo.findByMedicoId(idMedico)
                .map(ConfigAgenda::getDisponibilidadJson)
                .map(json -> tramosDelDia(json, dow))
                .orElse(List.of());
    }

    private List<Tramo> tramosDelDia(String disponibilidadJson, DayOfWeek day) {
        if (disponibilidadJson == null || disponibilidadJson.isBlank()) return List.of();
        try {
            Map<String, List<Map<String, String>>> raw =
                    mapper.readValue(disponibilidadJson, new TypeReference<>() {});
            String key = DAY_KEY.get(day);
            List<Map<String, String>> arr = raw.getOrDefault(key, List.of());
            List<Tramo> res = new ArrayList<>(arr.size());
            for (Map<String, String> obj : arr) {
                LocalTime desde = parseSafe(obj.get("desde"));
                LocalTime hasta = parseSafe(obj.get("hasta"));
                if (desde != null && hasta != null && desde.isBefore(hasta)) {
                    res.add(new Tramo(desde, hasta));
                }
            }
            res.sort(Comparator.comparing(t -> t.desde));
            return res;
        } catch (Exception e) {
            // JSON corrupto → devolvemos vacío para no romper la agenda
            return List.of();
        }
    }

    private LocalTime parseOrDefault(String hhmm, LocalTime def) {
        if (hhmm == null || hhmm.isBlank()) return def;
        try { return LocalTime.parse(hhmm.trim()); } catch (Exception e) { return def; }
    }

    private LocalTime parseSafe(String hhmm) {
        if (hhmm == null || hhmm.isBlank()) return null;
        try { return LocalTime.parse(hhmm.trim()); } catch (Exception e) { return null; }
    }

    private List<Slot> generarSlots(List<Tramo> tramos, int slotMin) {
        List<Slot> out = new ArrayList<>();
        Duration step = Duration.ofMinutes(slotMin);
        for (Tramo t : tramos) {
            LocalTime cur = t.desde;
            while (!cur.plus(step).isAfter(t.hasta)) {
                out.add(new Slot(cur, cur.plus(step)));
                cur = cur.plus(step);
            }
        }
        return out;
    }

    private boolean bloquea(Citas c) {
        return c.getEstado() != EstadoCita.CANCELADA;
    }

    private Slot toSlot(Citas c) {
        LocalTime ini = c.getHora();
        int dur = Math.max(5, c.getDuracionMinutos());
        return new Slot(ini, ini.plusMinutes(dur));
    }

    private boolean solapan(Slot a, Slot b) {
        return a.inicio.isBefore(b.fin) && b.inicio.isBefore(a.fin);
    }

    private record Tramo(LocalTime desde, LocalTime hasta) {}
    private record Slot(LocalTime inicio, LocalTime fin) {}
}