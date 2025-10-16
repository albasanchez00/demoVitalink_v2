package com.ceatformacion.demovitalink_v2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SintomasVistaController {
    @GetMapping("/registroSintomas")
    public String registroSintomas() {
        return "registroSintomas";
    }
}