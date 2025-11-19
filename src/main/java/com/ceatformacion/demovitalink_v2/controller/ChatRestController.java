package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.ArchiveRequest;
import com.ceatformacion.demovitalink_v2.dto.ConversacionDTO;
import com.ceatformacion.demovitalink_v2.dto.ConversacionDetalles;
import com.ceatformacion.demovitalink_v2.dto.MuteRequest;
import com.ceatformacion.demovitalink_v2.model.Conversacion;
import com.ceatformacion.demovitalink_v2.model.Mensaje;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ConversacionRepository;
import com.ceatformacion.demovitalink_v2.repository.MensajeRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ConversacionRepository convRepo;
    private final MensajeRepository mensajeRepo;  // ✅ AÑADIDO
    private final ChatService chat;
    private final UsuariosRepository usuariosRepo;

    public ChatRestController(ConversacionRepository convRepo,
                              ChatService chat, UsuariosRepository usuariosRepo, MensajeRepository mensajeRepo) {
        this.convRepo = convRepo;
        this.chat = chat;
        this.usuariosRepo = usuariosRepo;
        this.mensajeRepo = mensajeRepo;
    }

    /**
     * 📋 Lista las conversaciones del usuario autenticado
     * GET /api/chat/conversaciones
     */

    @GetMapping("/conversaciones")
    @Transactional(readOnly = true)
    public List<ConversacionRow> mias(
            Principal principal,
            @RequestParam(defaultValue = "false") Boolean archived  // ✅ AÑADIR
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());
        List<Conversacion> conversaciones;
        if (archived) {
            conversaciones = convRepo.findConversacionesArchivadas(userId);
        } else {
            conversaciones = convRepo.findConversacionesDeMiembroConFiltro(userId, false);
        }
        return conversaciones.stream()
                .map(c -> toRowWithUnread(c, userId))
                .collect(Collectors.toList());
    }

    /**
     * 💬 Histórico paginado de mensajes (solo si el usuario es miembro)
     * GET /api/chat/conversaciones/{id}/mensajes?page=0&size=50
     */
    @GetMapping("/conversaciones/{id}/mensajes")
    @Transactional(readOnly = true)
    public Page<MensajeRow> mensajes(
            Principal principal,
            @PathVariable Integer id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());

        if (!convRepo.pertenece(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No perteneces a esta conversación");
        }

        Page<Mensaje> mensajes = chat.historico(id, page, size);

        List<MensajeRow> dtos = mensajes.getContent().stream()
                .map(m -> new MensajeRow(
                        m.getId(),
                        chat.nombreParaMostrar(m.getRemitente()),
                        m.getContenido(),
                        m.getCreadoEn(),
                        chat.estaLeido(m.getId(), userId)
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, mensajes.getPageable(), mensajes.getTotalElements());
    }

    /**
     * ➕ Crear o reabrir conversación directa
     * POST /api/chat/conversaciones/directa?username=doctor123
     */
    @PostMapping("/conversaciones/directa")
    @Transactional
    public ResponseEntity<ConversacionDTO> crearDirecta(
            Principal principal,
            @RequestParam("username") String otroUsername
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        var me = usuariosRepo.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario actual no encontrado"
                ));

        var otro = usuariosRepo.findByUsernameIgnoreCase(otroUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario destino no encontrado"
                ));

        var conv = chat.getOrCreateDirectConversation(me, otro);
        return ResponseEntity.ok(ConversacionDTO.of(conv));
    }

    /**
     * 🗑️ Eliminar conversación (solo si eres miembro)
     * DELETE /api/chat/conversaciones/{id}
     */
    @DeleteMapping("/conversaciones/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarConversacion(
            @PathVariable Integer id,
            Principal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());
        chat.eliminarConversacion(id, userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 📖 Marcar todos los mensajes de una conversación como leídos
     * POST /api/chat/conversaciones/{id}/marcar-leida
     *
     * ✅ ENDPOINT QUE FALTABA
     */
    @PostMapping("/conversaciones/{id}/marcar-leida")
    @Transactional
    public ResponseEntity<Void> marcarConversacionLeida(
            @PathVariable Integer id,
            Principal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());

        // Verificar que el usuario es miembro de la conversación
        if (!convRepo.pertenece(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "No perteneces a esta conversación");
        }

        // Marcar todos los mensajes como leídos
        chat.marcarConversacionLeida(id, userId);

        return ResponseEntity.ok().build();
    }

    /**
     * 🔔 Obtener contador total de mensajes no leídos del usuario
     * GET /api/chat/no-leidos/total
     */
    @GetMapping("/no-leidos/total")
    @Transactional(readOnly = true)
    public UnreadCountResponse contadorNoLeidos(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());
        long total = chat.contarNoLeidosTotal(userId);

        return new UnreadCountResponse(total);
    }

    // ===== Mappers / DTOs internos =====
    private ConversacionRow toRowWithUnread(Conversacion c, Integer userId) {
        long unreadCount = chat.contarNoLeidosEnConversacion(c.getId(), userId);

        return new ConversacionRow(
                c.getId(),
                safe(c.getTipo()),
                safe(c.getServicio()),
                c.getMiembros() != null ? c.getMiembros().size() : 0,
                c.getCreadoEn(),
                unreadCount,
                c.getMuted() != null ? c.getMuted() : false,      // ✅ AÑADIDO
                c.getArchived() != null ? c.getArchived() : false // ✅ AÑADIDO
        );
    }

    public record MensajeRow(
            Integer id,
            String remitente,
            String contenido,
            LocalDateTime creadoEn,
            boolean leido
    ) {}

    public record ConversacionRow(
            Integer id,
            String tipo,
            String servicio,
            int miembrosCount,
            LocalDateTime creadoEn,
            long unreadCount,
            boolean muted,      // ✅ AÑADIDO
            boolean archived    // ✅ AÑADIDO
    ) {}

    public record UnreadCountResponse(long total) {}

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    @PutMapping("/conversaciones/{id}/silenciar")
    @Transactional
    public ResponseEntity<Void> silenciarConversacion(
            @PathVariable Integer id,
            @RequestBody MuteRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());

        if (!convRepo.pertenece(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        convRepo.setMuted(id, request.muted());
        return ResponseEntity.ok().build();
    }

    /**
     * 📁 Archivar/Desarchivar conversación
     * PUT /api/chat/conversaciones/{id}/archivar
     * Body: { "archived": true }
     */
    @PutMapping("/conversaciones/{id}/archivar")
    @Transactional
    public ResponseEntity<Void> archivarConversacion(
            @PathVariable Integer id,
            @RequestBody ArchiveRequest request,
            Principal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());

        if (!convRepo.pertenece(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        convRepo.setArchived(id, request.archived());
        return ResponseEntity.ok().build();
    }

    /**
     * 🗑️ Limpiar historial de conversación
     * DELETE /api/chat/conversaciones/{id}/historial
     */
    @DeleteMapping("/conversaciones/{id}/historial")
    @Transactional
    public ResponseEntity<Void> limpiarHistorial(
            @PathVariable Integer id,
            Principal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());

        if (!convRepo.pertenece(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        mensajeRepo.deleteByConversacionId(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * ℹ️ Obtener detalles completos de una conversación
     * GET /api/chat/conversaciones/{id}/detalles
     */
    @GetMapping("/conversaciones/{id}/detalles")
    @Transactional(readOnly = true)
    public ResponseEntity<ConversacionDetalles> obtenerDetalles(
            @PathVariable Integer id,
            Principal principal
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());

        if (!convRepo.pertenece(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Conversacion conv = convRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long totalMensajes = mensajeRepo.countByConversacion_Id(id);
        long noLeidos = chat.contarNoLeidosEnConversacion(id, userId);

        List<String> miembros = conv.getMiembros().stream()
                .map(u -> chat.nombreParaMostrar(u))
                .collect(Collectors.toList());

        return ResponseEntity.ok(new ConversacionDetalles(
                conv.getId(),
                conv.getTipo(),
                conv.getServicio(),
                conv.getMuted(),
                conv.getArchived(),
                conv.getCreadoEn(),
                miembros,
                totalMensajes,
                noLeidos
        ));
    }

    /**
     * 🔍 Buscar mensajes en una conversación
     * GET /api/chat/conversaciones/{id}/buscar?q=hola&page=0&size=20
     */
    @GetMapping("/conversaciones/{id}/buscar")
    @Transactional(readOnly = true)
    public Page<MensajeRow> buscar(
            Principal principal,
            @PathVariable Integer id,
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());

        if (!convRepo.pertenece(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        Page<Mensaje> mensajes = mensajeRepo.buscarEnConversacion(
                id,
                q,
                org.springframework.data.domain.PageRequest.of(page, size)
        );

        List<MensajeRow> dtos = mensajes.getContent().stream()
                .map(m -> new MensajeRow(
                        m.getId(),
                        chat.nombreParaMostrar(m.getRemitente()),
                        m.getContenido(),
                        m.getCreadoEn(),
                        chat.estaLeido(m.getId(), userId)
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, mensajes.getPageable(), mensajes.getTotalElements());
    }
}