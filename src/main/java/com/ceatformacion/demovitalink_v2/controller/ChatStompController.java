package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.ChatMessageDTO;
import com.ceatformacion.demovitalink_v2.dto.MensajeDTO;
import com.ceatformacion.demovitalink_v2.services.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
public class ChatStompController {

    private final SimpMessagingTemplate template;
    private final ChatService chat;

    public ChatStompController(SimpMessagingTemplate t, ChatService c) {
        this.template = t;
        this.chat = c;
    }

    /**
     * 💬 Enviar mensaje a una conversación
     * Endpoint: /app/chat.send
     *
     * ✅ FLUJO CORRECTO:
     * 1. Guardar en BD
     * 2. Broadcast a TODOS (incluido remitente)
     * 3. Frontend del remitente lo filtra (ya lo mostró optimista)
     * 4. Otros usuarios lo reciben y lo muestran
     */
    @MessageMapping("/chat.send")
    public void enviar(@Payload ChatMessageDTO dto, Principal principal) {
        Integer remitenteId = chat.obtenerIdDesdePrincipal(principal.getName());

        // 1️⃣ Guardar mensaje en BD
        MensajeDTO out = chat.publicarYMapear(dto.convId(), remitenteId, dto.texto());

        // 2️⃣ Broadcasting a TODOS los suscritos (incluido remitente)
        // ✅ CORRECCIÓN: Usar convertAndSend para que TODOS reciban
        // El frontend decide si mostrarlo o no
        template.convertAndSend("/topic/conversaciones." + out.convId(), out);

        // 3️⃣ Confirmación individual al remitente (opcional)
        template.convertAndSendToUser(
                principal.getName(),
                "/queue/ack",
                new AckResponse("DELIVERED", out.id())
        );
    }

    /**
     * 🖊️ Indicador "está escribiendo..."
     * Endpoint: /app/chat.typing
     */
    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingDTO dto, Principal principal) {
        // Broadcasting a todos (cada frontend decide si mostrarlo)
        template.convertAndSend(
                "/topic/conversaciones." + dto.convId() + ".typing",
                new TypingIndicator(principal.getName(), dto.isTyping())
        );
    }

    /**
     * 📖 Marcar mensaje como leído
     * Endpoint: /app/chat.markRead
     */
    @MessageMapping("/chat.markRead")
    public void markRead(@Payload ReadReceiptDTO dto, Principal principal) {
        Integer userId = chat.obtenerIdDesdePrincipal(principal.getName());
        chat.marcarLeido(dto.mensajeId(), userId);

        // Notificar a otros miembros
        template.convertAndSend(
                "/topic/conversaciones." + dto.convId() + ".read",
                new ReadNotification(dto.mensajeId(), principal.getName())
        );
    }

    // ===== DTOs internos =====

    record AckResponse(String status, Integer messageId) {}

    record TypingDTO(Integer convId, boolean isTyping) {}

    record TypingIndicator(String username, boolean isTyping) {}

    record ReadReceiptDTO(Integer convId, Integer mensajeId) {}

    record ReadNotification(Integer mensajeId, String reader) {}
}