package com.ceatformacion.demovitalink_v2.controller;

import com.ceatformacion.demovitalink_v2.dto.ChatMessageDTO;
import com.ceatformacion.demovitalink_v2.dto.MensajeDTO;
import com.ceatformacion.demovitalink_v2.services.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
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

    @MessageMapping("/chat.send")
    public void enviar(ChatMessageDTO dto, Principal principal) {
        Integer remitenteId = chat.obtenerIdDesdePrincipal(principal.getName());
        MensajeDTO out = chat.publicarYMapear(dto.convId(), remitenteId, dto.texto());
        template.convertAndSend("/topic/conversaciones." + out.convId(), out);
        template.convertAndSendToUser(principal.getName(), "/queue/ack", "DELIVERED");
    }
}