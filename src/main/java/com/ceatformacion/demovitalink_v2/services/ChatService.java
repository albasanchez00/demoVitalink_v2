package com.ceatformacion.demovitalink_v2.services;

import com.ceatformacion.demovitalink_v2.dto.MensajeDTO;
import com.ceatformacion.demovitalink_v2.model.Conversacion;
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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ChatService {

    private final ConversacionRepository convRepo;
    private final MensajeRepository msgRepo;
    private final UsuariosRepository usuariosRepo;
    private final LecturaRepository lecturaRepo;

    public ChatService(ConversacionRepository c,
                       MensajeRepository m,
                       UsuariosRepository u,
                       LecturaRepository l) {
        this.convRepo = c;
        this.msgRepo = m;
        this.usuariosRepo = u;
        this.lecturaRepo = l;
    }

    /**
     * 🔐 Convierte username -> ID de usuario
     */
    public Integer obtenerIdDesdePrincipal(String principalName) {
        return usuariosRepo.findByUsernameIgnoreCase(principalName)
                .map(Usuarios::getId_usuario)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No existe usuario con username=" + principalName
                ));
    }

    /**
     * 👥 Obtiene los usernames de todos los miembros de una conversación
     */
    public List<String> obtenerUsernamesMiembros(Integer convId) {
        return convRepo.findById(convId)
                .map(conv -> conv.getMiembros()
                        .stream()
                        .map(Usuarios::getUsername)
                        .toList())
                .orElse(List.of());
    }

    /**
     * 💬 Crear o recuperar conversación directa entre dos usuarios
     */
    @Transactional
    public Conversacion getOrCreateDirectConversation(Usuarios a, Usuarios b) {
        int idA = a.getId_usuario();
        int idB = b.getId_usuario();

        // Clave determinística: menorID-mayorID
        String key = (idA < idB) ? idA + "-" + idB : idB + "-" + idA;

        return convRepo.findByTipoAndDirectKey("DIRECT", key)
                .orElseGet(() -> {
                    Conversacion c = new Conversacion();
                    c.setTipo("DIRECT");
                    c.setDirectKey(key);
                    c.setServicio("CHAT");
                    c.setCreadoPor(a);

                    Set<Usuarios> miembros = new HashSet<>();
                    miembros.add(a);
                    miembros.add(b);
                    c.setMiembros(miembros);

                    return convRepo.save(c);
                });
    }

    /**
     * 🗑️ Elimina conversación (verificando permisos)
     */
    @Transactional
    public void eliminarConversacion(Integer id, Integer userId) {
        boolean pertenece = convRepo.existsByIdAndMiembro(id, userId);
        if (!pertenece) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No puedes eliminar una conversación ajena");
        }
        convRepo.deleteByIdHard(id);
    }

    /**
     * ✉️ Publica mensaje en conversación y retorna DTO mapeado
     */
    @Transactional
    public MensajeDTO publicarYMapear(Integer convId, Integer remitenteId, String texto) {
        var conv = convRepo.findById(convId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Conversación no encontrada"
                ));

        var remitente = usuariosRepo.findByIdWithCliente(remitenteId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Usuario no encontrado: " + remitenteId
                ));

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

    /**
     * 📜 Histórico paginado de mensajes de una conversación
     */
    public Page<Mensaje> historico(Integer convId, int page, int size) {
        return msgRepo.findByConversacion_Id(
                convId,
                PageRequest.of(page, size, Sort.Direction.DESC, "creadoEn")
        );
    }

    /**
     * 👤 Construye nombre para mostrar del usuario
     *
     * ✅ CORREGIDO: Ahora PRIORIZA el username
     *
     * Orden de prioridad:
     * 1. Username (SIEMPRE presente)
     * 2. Nombre completo del cliente (si existe)
     * 3. Email del cliente (si existe)
     * 4. Fallback: "Usuario"
     */
    public String nombreParaMostrar(Usuarios u) {
        // ✅ PRIORIDAD 1: Username (RECOMENDADO para chat)
        try {
            String username = safe(u.getUsername());
            if (!username.isBlank()) {
                return username;  // 👈 RETORNA DIRECTAMENTE EL USERNAME
            }
        } catch (Exception ignored) {}

        // Solo si NO hay username, intentar otras opciones
        try {
            if (u.getCliente() != null) {
                var c = u.getCliente();

                // Intentar nombre completo
                String n = safe(c.getNombre());
                String a = safe(c.getApellidos());
                String full = (n + " " + a).trim();
                if (!full.isBlank()) return full;

                // Intentar correo
                String correo = safe(c.getCorreoElectronico());
                if (!correo.isBlank()) return correo;
            }
        } catch (Exception ignored) {}

        // Fallback final
        return "Usuario";
    }

    // ========== SISTEMA DE LECTURAS ==========

    /**
     * 📖 Marca un mensaje como leído por un usuario
     */
    @Transactional
    public void marcarLeido(Integer mensajeId, Integer usuarioId) {
        if (!lecturaRepo.existsLectura(mensajeId, usuarioId)) {
            var lectura = new Lectura();
            lectura.setMensaje(msgRepo.getReferenceById(mensajeId));
            lectura.setUsuario(usuariosRepo.getReferenceById(usuarioId));
            lecturaRepo.save(lectura);
        }
    }

    /**
     * 📖 Marca todos los mensajes de una conversación como leídos
     */
    @Transactional
    public void marcarConversacionLeida(Integer convId, Integer usuarioId) {
        var mensajes = msgRepo.findByConversacion_Id(
                convId,
                PageRequest.of(0, Integer.MAX_VALUE)
        );

        mensajes.forEach(m -> marcarLeido(m.getId(), usuarioId));
    }

    /**
     * 🔍 Verifica si un mensaje fue leído por un usuario
     */
    public boolean estaLeido(Integer mensajeId, Integer usuarioId) {
        return lecturaRepo.existsLectura(mensajeId, usuarioId);
    }

    /**
     * 🔢 Cuenta mensajes no leídos en una conversación específica
     */
    @Transactional(readOnly = true)
    public long contarNoLeidosEnConversacion(Integer convId, Integer usuarioId) {
        return convRepo.contarNoLeidosEnConversacion(convId, usuarioId);
    }

    /**
     * 🔢 Cuenta TODOS los mensajes no leídos del usuario (en todas sus conversaciones)
     */
    @Transactional(readOnly = true)
    public long contarNoLeidosTotal(Integer usuarioId) {
        return convRepo.contarNoLeidosTotales(usuarioId);
    }

    // ===== Utilidades =====

    private static String safe(String s) {
        return s == null ? "" : s;
    }
}