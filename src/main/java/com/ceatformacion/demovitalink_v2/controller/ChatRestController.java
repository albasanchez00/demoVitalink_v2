package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.ConversacionDTO;
import com.ceatformacion.demovitalink_v2.model.Conversacion;
import com.ceatformacion.demovitalink_v2.model.Mensaje;
import com.ceatformacion.demovitalink_v2.model.Usuarios;
import com.ceatformacion.demovitalink_v2.repository.ConversacionRepository;
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
    private final ChatService chat;
    private final UsuariosRepository usuariosRepo;

    public ChatRestController(ConversacionRepository convRepo,
                              ChatService chat,
                              UsuariosRepository usuariosRepo) {
        this.convRepo = convRepo;
        this.chat = chat;
        this.usuariosRepo = usuariosRepo;
    }

    /** Lista las conversaciones del usuario autenticado */
    @GetMapping("/conversaciones")
    @Transactional(readOnly = true)
    public List<ConversacionRow> mias(Principal principal) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());
        List<Conversacion> conversaciones = convRepo.findConversacionesDeMiembro(userId);
        return conversaciones.stream().map(this::toRow).collect(Collectors.toList());
    }

    /** Histórico paginado de mensajes (solo si el usuario es miembro) */
    @GetMapping("/conversaciones/{id}/mensajes")
    @Transactional(readOnly = true)
    public Page<MensajeRow> mensajes(Principal principal,
                                     @PathVariable Integer id,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "50") int size) {
        if (principal == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());
        if (!convRepo.pertenece(id, userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No perteneces a esta conversación");
        }

        Page<Mensaje> mensajes = chat.historico(id, page, size);
        List<MensajeRow> dtos = mensajes.getContent().stream()
                .map(m -> new MensajeRow(
                        m.getId(),
                        chat.nombreParaMostrar(m.getRemitente()),
                        m.getContenido(),
                        m.getCreadoEn()
                ))
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, mensajes.getPageable(), mensajes.getTotalElements());
    }

    @PostMapping("/conversaciones/directa")
    @Transactional
    public ResponseEntity<ConversacionDTO> crearDirecta(Principal principal,
                                                        @RequestParam("username") String otroUsername) {
        if (principal == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        var me = usuariosRepo.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario actual no encontrado"));

        var otro = usuariosRepo.findByUsernameIgnoreCase(otroUsername)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario destino no encontrado"));

        var conv = chat.getOrCreateDirectConversation(me, otro);
        return ResponseEntity.ok(ConversacionDTO.of(conv));
    }


    // --- Mappers / DTOs internos del controlador ---
    private ConversacionRow toRow(Conversacion c) {
        return new ConversacionRow(
                c.getId(),
                safe(c.getTipo()),
                safe(c.getServicio()),
                c.getMiembros() != null ? c.getMiembros().size() : 0,
                c.getCreadoEn()
        );
    }

    public record MensajeRow(Integer id, String remitente, String contenido, LocalDateTime creadoEn) {}
    public record ConversacionRow(Integer id, String tipo, String servicio, int miembrosCount, LocalDateTime creadoEn) {}

    private static String safe(String s) { return s == null ? "" : s; }

    @DeleteMapping("/conversaciones/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarConversacion(@PathVariable Integer id, Principal principal) {
        if (principal == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);

        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());
        chat.eliminarConversacion(id, userId);
        return ResponseEntity.noContent().build();
    }

}