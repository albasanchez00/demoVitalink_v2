package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.EventoCalendarDTO;
import com.ceatformacion.demovitalink_v2.model.Citas;
import com.ceatformacion.demovitalink_v2.model.EstadoCita;
import com.ceatformacion.demovitalink_v2.model.Rol;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.CitasRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.CitasService;
import com.ceatformacion.demovitalink_v2.services.UsuariosDetails;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/medico")
public class CitasMedicoController {

    private final CitasService citasService;
    private final UsuariosRepository usuariosRepository;
    private final CitasRepository citasRepository;

    public CitasMedicoController(CitasService citasService,
                                 UsuariosRepository usuariosRepository,
                                 CitasRepository citasRepository) {
        this.citasService = citasService;
        this.usuariosRepository = usuariosRepository;
        this.citasRepository = citasRepository;
    }

    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    @GetMapping("/citas")
    @Transactional(readOnly = true)
    public String agendaMedico(@AuthenticationPrincipal UsuariosDetails principal,
                               @RequestParam(value = "desde", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                               @RequestParam(value = "hasta", required = false)
                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                               @RequestParam(value = "medicoId", required = false) Integer medicoId,
                               @RequestParam(value = "estado", required = false) EstadoCita estado,
                               Model model) {

        LocalDate hoy = LocalDate.now();
        if (desde == null) desde = hoy;
        if (hasta == null) hasta = hoy.plusDays(30);

        Usuarios medico = resolveMedicoConCliente(principal, medicoId);
        if (medico == null) {
            model.addAttribute("error", "No se pudo determinar el médico actual.");
            return "agendaCitas";
        }

        List<Citas> citas = citasService.listarAgendaDelMedico(
                medico.getId_usuario(), desde, hasta, estado
        );

        // nombre “seguro” para la cabecera (evita navegar LAZY en la vista)
        String medicoNombre = (medico.getCliente() != null && medico.getCliente().getNombre() != null)
                ? (medico.getCliente().getNombre() + " " +
                (medico.getCliente().getApellidos() != null ? medico.getCliente().getApellidos() : ""))
                : medico.getUsername();

        model.addAttribute("citas", citas);
        model.addAttribute("medicoActual", medico);
        model.addAttribute("medicoNombre", medicoNombre);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);
        model.addAttribute("estado", estado);

        return "agendaCitas";
    }

    private Usuarios resolveMedicoConCliente(UsuariosDetails principal, Integer medicoId) {
        Usuarios yo = principal.getUsuario();

        if (yo.getRol() == Rol.ADMIN && medicoId != null) {
            return usuariosRepository.findByIdWithCliente(medicoId).orElse(null);
        }
        // si es MEDICO (o ADMIN sin medicoId), cargar su propio usuario con cliente
        return usuariosRepository.findByIdWithCliente(yo.getId_usuario()).orElse(yo);
    }

    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    @PostMapping("/citas/{id}/confirmar")
    @Transactional
    public String confirmarCita(@PathVariable int id, RedirectAttributes ra) {
        return actualizarEstado(id, EstadoCita.CONFIRMADA, ra);
    }

    @PreAuthorize("hasAnyRole('MEDICO','ADMIN')")
    @PostMapping("/citas/{id}/cancelar")
    @Transactional
    public String cancelarCita(@PathVariable int id, RedirectAttributes ra) {
        return actualizarEstado(id, EstadoCita.CANCELADA, ra);
    }

    private String actualizarEstado(int id, EstadoCita nuevoEstado, RedirectAttributes ra) {
        Optional<Citas> opt = citasRepository.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Cita no encontrada.");
        } else {
            Citas c = opt.get();
            c.setEstado(nuevoEstado);
            citasRepository.save(c);
            ra.addFlashAttribute("mensaje", "Cita " + nuevoEstado.name().toLowerCase() + ".");
        }
        return "redirect:/medico/citas";
    }
}