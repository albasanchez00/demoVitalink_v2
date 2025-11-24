package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.EventoCalendarDTO;
import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.EstadoCita;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
public class CitasService {
    @Autowired
    private CitasRepository citasRepository;

    public void guardarCita(Citas cita) {
        citasRepository.save(cita);
    }

    public List<Citas> obtenerCitasPorUsuario(Usuarios usuario) {
        return citasRepository.findCitasByUsuario(usuario);
    }

    // ===== NUEVO: Obtener cita por ID =====
    public Optional<Citas> obtenerPorId(int idCita) {
        return citasRepository.findById(idCita);
    }

    // ===== NUEVO: Eliminar cita =====
    public void eliminar(int idCita) {
        citasRepository.deleteById(idCita);
    }

    // === NUEVO: agenda por médico (devuelve entidades) ===
    public List<Citas> listarAgendaDelMedico(int idMedico,
                                             LocalDate desde,
                                             LocalDate hasta,
                                             EstadoCita estado) {
        if (estado == null) {
            return citasRepository.findAgendaMedicoBetween(idMedico, desde, hasta);
        }
        return citasRepository.findAgendaMedicoByEstado(idMedico, estado, desde, hasta);
    }

    // === NUEVO: agenda por médico formateada para calendario ===
    public List<EventoCalendarDTO> listarEventosAgendaDelMedico(int idMedico,
                                                                LocalDate desde,
                                                                LocalDate hasta,
                                                                EstadoCita estado) {
        return listarAgendaDelMedico(idMedico, desde, hasta, estado)
                .stream()
                .map(this::toEventoCalendarDTO)
                .toList();
    }

    // === NUEVO: métricas ===
    public long contarCitasDeHoy(int idMedico) {
        return citasRepository.countHoyPorMedico(idMedico, LocalDate.now());
    }

    public long contarCitasEntre(int idMedico, LocalDate desde, LocalDate hasta) {
        return citasRepository.countEntreFechasPorMedico(idMedico, desde, hasta);
    }

    // ====== helper mapeo ======
    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private EventoCalendarDTO toEventoCalendarDTO(Citas c) {
        LocalDateTime start = LocalDateTime.of(c.getFecha(), c.getHora());
        LocalDateTime end = start.plusMinutes(c.getDuracionMinutos());

        Integer pacienteId = (c.getUsuario() != null) ? c.getUsuario().getId_usuario() : null;
        String pacienteNombre = (c.getUsuario() != null) ? safeNombre(c.getUsuario()) : null;

        String titulo = (c.getTitulo() != null && !c.getTitulo().isBlank())
                ? c.getTitulo()
                : "Cita";

        return new EventoCalendarDTO(
                c.getId_cita(),
                titulo,
                start.format(ISO),
                end.format(ISO),
                c.getEstado().name(),
                pacienteId,
                pacienteNombre,
                c.getDescripcion()
        );
    }

    private static String safeNombre(Usuarios u) {
        // Ajusta si tu entidad Usuarios no tiene nombre/apellidos
        // (si lo tienes en Clientes, puedes obtenerlo por u.getCliente().getNombre() con cuidado LAZY)
        return u.getUsername(); // fallback razonable si no hay nombre real
    }

}