package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.model.PanelUsuarioVM;
import com.ceatformacion.demovitalink_v2.model.Recordatorios;
import com.ceatformacion.demovitalink_v2.model.TipoRecordatorio;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

@Service
public class PanelUsuarioService {

    private final CitasRepository citasRepo;
    private final TratamientosRepository tratRepo;
    private final RecordatoriosRepository recRepo;
    private final MensajeRepository msgRepo;
    private final UsuariosRepository usuariosRepo;

    private static final DateTimeFormatter FECHA_HORA =
            DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("es", "ES"));

    public PanelUsuarioService(
            CitasRepository citasRepo,
            TratamientosRepository tratRepo,
            RecordatoriosRepository recRepo,
            MensajeRepository msgRepo,
            UsuariosRepository usuariosRepo
    ) {
        this.citasRepo = citasRepo;
        this.tratRepo = tratRepo;
        this.recRepo = recRepo;
        this.msgRepo = msgRepo;
        this.usuariosRepo = usuariosRepo;
    }

    @Transactional(readOnly = true)
    public PanelUsuarioVM cargarPanel(String username) {
        PanelUsuarioVM vm = new PanelUsuarioVM();

        // 1️⃣ Buscar el usuario actual
        Usuarios user = usuariosRepo.findByUsername(username)
                .orElse(null);
        if (user == null) return vm;

        int userId = user.getId_usuario();

        // 2️⃣ Datos reales
        vm.tratamientosEnCurso = safe(() -> (int) tratRepo.countByUsuario(userId), 0);
        vm.citasPasadas        = safe(() -> (int) citasRepo.countByUsuario(userId), 0);

        // 🔔 Próximo medicamento (Recordatorio tipo MEDICAMENTO)
        List<Recordatorios> meds = recRepo.findAllByUsuarioIdAndTipo(userId, TipoRecordatorio.MEDICAMENTO);
        if (!meds.isEmpty()) {
            Recordatorios r = meds.get(0);
            vm.proximoMedicamento = r.getTitulo();
            vm.proximaDosisHoraFmt = r.getFechaHora().format(FECHA_HORA);
        } else {
            vm.proximoMedicamento = null;
            vm.proximaDosisHoraFmt = null;
        }

        // 🏥 Próxima cita (Recordatorio tipo CITA o de la tabla Citas)
        List<Recordatorios> citasRec = recRepo.findAllByUsuarioIdAndTipo(userId, TipoRecordatorio.CITA);
        if (!citasRec.isEmpty()) {
            Recordatorios c = citasRec.get(0);
            vm.proximaCitaFmt = c.getFechaHora().format(FECHA_HORA);
        } else {
            // Si no hay recordatorio, usa la próxima cita real
            LocalDate proxima = safe(() -> citasRepo.minFecha(userId), null);
            vm.proximaCitaFmt = (proxima != null) ? proxima.format(FECHA_HORA) : "—";
        }

        // ⚠️ Alertas de dosis olvidadas (recordatorios vencidos no completados)
        vm.alertasDosis = safe(() -> {
            return (int) recRepo.findAllByUsuarioId(userId)
                    .stream()
                    .filter(r -> !r.isCompletado() && r.getFechaHora().isBefore(LocalDate.now().atStartOfDay()))
                    .count();
        }, 0);

        // 💬 Mensajes (no tienes campo leido → se deja total de mensajes por ahora)
        vm.unread = safe(() -> (int) msgRepo.count(), 0);

        // ⚙️ Configuración (perfil)
        vm.perfilCompleto = user.getCliente() != null;

        // Valores que no tienes modelos aún
        vm.informes = 0;
        vm.notifs = 0;
        vm.adherenciaPorc = 0;

        return vm;
    }

    private static <T> T safe(Supplier<T> s, T def) {
        try { return s.get(); } catch (Exception e) { return def; }
    }
}
