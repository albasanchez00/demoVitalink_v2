package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.MensajeDTO;
import com.ceatformacion.demovitalink_v2.model.Lectura;
import com.ceatformacion.demovitalink_v2.model.Mensaje;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ConversacionRepository;
import com.ceatformacion.demovitalink_v2.repository.LecturaRepository;
import com.ceatformacion.demovitalink_v2.repository.MensajeRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChatService {

    private final ConversacionRepository convRepo;
    private final MensajeRepository msgRepo;
    private final UsuariosRepository usuariosRepo;
    private final LecturaRepository lecturaRepo; // <-- NUEVO

    public ChatService(ConversacionRepository c,
                       MensajeRepository m,
                       UsuariosRepository u,
                       LecturaRepository l) {     // <-- NUEVO
        this.convRepo = c;
        this.msgRepo = m;
        this.usuariosRepo = u;
        this.lecturaRepo = l;                     // <-- NUEVO
    }


    /** Convierte principal.getName() (username) -> id de Usuarios. */
    public Integer obtenerIdDesdePrincipal(String principalName) {
        return usuariosRepo.findByUsernameIgnoreCase(principalName)
                .map(this::getUsuarioId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe usuario con username=" + principalName));
    }

    /** Publica el mensaje y devuelve DTO ya mapeado con nombre listo. */
    @Transactional
    public MensajeDTO publicarYMapear(Integer convId, Integer remitenteId, String texto) {
        var conv = convRepo.findById(convId).orElseThrow();

        // Carga remitente + cliente (para nombre/apellidos)
        var remitente = usuariosRepo.findByIdWithCliente(remitenteId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado: " + remitenteId));

        var msg = new Mensaje();
        msg.setConversacion(conv);
        msg.setRemitente(remitente);
        msg.setContenido(texto);
        var saved = msgRepo.save(msg);

        return new MensajeDTO(
                saved.getId(),
                convId,
                nombreParaMostrar(remitente),
                saved.getContenido(),
                saved.getTipo(),
                saved.getUrlAdjunto(),
                saved.getCreadoEn()
        );
    }

    public Page<Mensaje> historico(Integer convId, int page, int size) {
        return msgRepo.findByConversacion_Id(
                convId, PageRequest.of(page, size, Sort.Direction.DESC, "creadoEn"));
    }

    /** Construye nombre: "Nombre Apellidos" -> email cliente -> username -> "Usuario". */
    public String nombreParaMostrar(Usuarios u) {
        try {
            if (u.getCliente() != null) {
                var c = u.getCliente();
                String n = safe(c.getNombre());
                String a = safe(c.getApellidos());
                String full = (n + " " + a).trim();
                if (!full.isBlank()) return full;

                String correo = safe(c.getCorreoElectronico());
                if (!correo.isBlank()) return correo;
            }
        } catch (Exception ignored) {}
        try {
            String user = safe(u.getUsername());
            if (!user.isBlank()) return user;
        } catch (Exception ignored) {}
        return "Usuario";
    }

    private static String safe(String s) { return s == null ? "" : s; }

    /** ⚠️ AJUSTA AQUÍ el getter exacto de tu PK en Usuarios */
    private Integer getUsuarioId(Usuarios u) {
        // Si tu getter real es getId_usuario():
        return u.getId_usuario();
        // Si fuera getIdUsuario(), usa:
        // return u.getIdUsuario();
    }

    @Transactional
    public void marcarLeido(Integer mensajeId, Integer usuarioId) {
        if (!lecturaRepo.existsLectura(mensajeId, usuarioId)) {
            var l = new Lectura();
            l.setMensaje(msgRepo.getReferenceById(mensajeId));
            l.setUsuario(usuariosRepo.getReferenceById(usuarioId));
            lecturaRepo.save(l);
        }
    }
}