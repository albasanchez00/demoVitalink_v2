package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/medico")
public class ChatVistaController {

    @GetMapping("/chat")
    public String abrirChatMedico() {
        return "chatMedico"; // plantilla
    }
}
