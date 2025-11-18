package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.ConversacionDTO;
import com.ceatformacion.demovitalink_v2.dto.MensajeDTO;
import com.ceatformacion.demovitalink_v2.model.Conversacion;
import com.ceatformacion.demovitalink_v2.model.Mensaje;
import com.ceatformacion.demovitalink_v2.repository.ConversacionRepository;
import com.ceatformacion.demovitalink_v2.repository.MensajeRepository;
import com.ceatformacion.demovitalink_v2.repository.UsuariosRepository;
import com.ceatformacion.demovitalink_v2.services.ChatService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;

@RestController
@RequestMapping("/api/admin/chat")
@PreAuthorize("hasRole('ADMIN')")
public class AdminChatRestController {

    private final ConversacionRepository convRepo;
    private final MensajeRepository msgRepo;
    private final UsuariosRepository usuariosRepo;
    private final ChatService chatService;

    public AdminChatRestController(ConversacionRepository convRepo,
                                   MensajeRepository msgRepo,
                                   UsuariosRepository usuariosRepo,
                                   ChatService chatService) {
        this.convRepo = convRepo;
        this.msgRepo = msgRepo;
        this.usuariosRepo = usuariosRepo;
        this.chatService = chatService;
    }

    /**
     * 📋 Listar TODAS las conversaciones (con filtros)
     * GET /api/admin/chat/conversaciones?q=busqueda&tipo=DIRECT&page=0&size=20
     */
    @GetMapping(value = "/conversaciones", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public Page<ConversacionDTO> listarGlobal(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tipo,
            Pageable pageable
    ) {
        Page<Conversacion> page = convRepo.buscarGlobal(q, tipo, pageable);
        return page.map(ConversacionDTO::of);
    }

    /**
     * 💬 Mensajes de una conversación (sin restricciones de membresía)
     * GET /api/admin/chat/conversaciones/{convId}/mensajes?page=0&size=50
     */
    @GetMapping(value = "/conversaciones/{convId}/mensajes", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public Page<MensajeDTO> mensajes(
            @PathVariable Integer convId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Page<Mensaje> data = msgRepo.findByConversacion_Id(
                convId,
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "creadoEn"))
        );
        return data.map(m -> new MensajeDTO(
                m.getId(),
                convId,
                chatService.nombreParaMostrar(m.getRemitente()),
                m.getContenido(),
                m.getTipo(),
                m.getUrlAdjunto(),
                m.getCreadoEn()
        ));
    }

    /**
     * ✉️ Enviar mensaje como ADMIN a cualquier conversación
     * POST /api/admin/chat/conversaciones/{convId}/mensajes
     * Content-Type: text/plain
     */
    @PostMapping(value = "/conversaciones/{convId}/mensajes", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Transactional
    public MensajeDTO enviar(
            @PathVariable Integer convId,
            @RequestBody String texto,
            Principal principal
    ) {
        Integer adminId = chatService.obtenerIdDesdePrincipal(principal.getName());
        return chatService.publicarYMapear(convId, adminId, texto);
    }

    /**
     * ➕ Crear o reabrir conversación directa (NUEVO - antes faltaba)
     * POST /api/admin/chat/conversaciones/directa?username=doctor123
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

        var admin = usuariosRepo.findByUsernameIgnoreCase(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Admin no encontrado"
                ));

        var otro = usuariosRepo.findByUsernameIgnoreCase(otroUsername)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Usuario destino no encontrado: " + otroUsername
                ));

        var conv = chatService.getOrCreateDirectConversation(admin, otro);
        return ResponseEntity.ok(ConversacionDTO.of(conv));
    }

    /**
     * 🗑️ Eliminar conversación (admin puede eliminar cualquiera)
     * DELETE /api/admin/chat/conversaciones/{id}
     */
    @DeleteMapping("/conversaciones/{id}")
    @Transactional
    public ResponseEntity<Void> eliminarConversacion(@PathVariable Integer id) {
        if (!convRepo.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Conversación no encontrada");
        }
        convRepo.deleteByIdHard(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 📊 Estadísticas de una conversación
     * GET /api/admin/chat/conversaciones/{id}/stats
     */
    @GetMapping("/conversaciones/{id}/stats")
    @Transactional(readOnly = true)
    public ConversacionStats obtenerEstadisticas(@PathVariable Integer id) {
        var conv = convRepo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        long totalMensajes = msgRepo.countByConversacion_Id(id);

        return new ConversacionStats(
                conv.getId(),
                conv.getTipo(),
                conv.getMiembros() != null ? conv.getMiembros().size() : 0,
                totalMensajes,
                conv.getCreadoEn()
        );
    }

    // ===== DTOs internos =====

    record ConversacionStats(
            Integer id,
            String tipo,
            int miembros,
            long totalMensajes,
            java.time.LocalDateTime creadoEn
    ) {}
}