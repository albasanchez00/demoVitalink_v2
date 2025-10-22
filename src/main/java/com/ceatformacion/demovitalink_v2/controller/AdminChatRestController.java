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
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

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

    /** 🔍 Listar TODAS las conversaciones (con filtros q, tipo, pageable) */
    @GetMapping(value="/conversaciones", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional(readOnly = true)
    public Page<ConversacionDTO> listarGlobal(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String tipo,
            Pageable pageable
    ) {
        Page<Conversacion> page = convRepo.buscarGlobal(q, tipo, pageable);
        return page.map(ConversacionDTO::of);
    }

    /** 💬 Mensajes de una conversación (sin restricciones de membresía) */
    @GetMapping(value="/conversaciones/{convId}/mensajes", produces = MediaType.APPLICATION_JSON_VALUE)
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

    /** ✉️ Enviar mensaje como ADMIN a cualquier conversación */
    @PostMapping(value="/conversaciones/{convId}/mensajes", consumes = MediaType.TEXT_PLAIN_VALUE)
    @Transactional
    public MensajeDTO enviar(
            @PathVariable Integer convId,
            @RequestBody String texto,
            Principal principal
    ) {
        Integer adminId = chatService.obtenerIdDesdePrincipal(principal.getName());
        return chatService.publicarYMapear(convId, adminId, texto);
    }
}